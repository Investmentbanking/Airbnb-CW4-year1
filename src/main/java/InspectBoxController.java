import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller class for the FXML pane that contains almost all the information of a specific listing
 * in the form of a visual panel of information.
 *
 * @author Cosmo Colman (K21090628)
 * @version 26.03.2022
 */
public class InspectBoxController implements Initializable {

    @FXML private VBox root;

    // 1st Section Nodes
    @FXML private Label rating;
    @FXML private Label id, name;

    // 2nd Section Nodes
    @FXML private StackPane image_holder;
    @FXML private ProgressIndicator picture_url_loading;
    @FXML private ImageView picture_url;
    @FXML private Label property_type;

    // 3rd Section Nodes
    @FXML private ProgressIndicator host_picture_url_loading;
    @FXML private ImageView host_picture_url;
    @FXML private Label host_id, host_name, price;

    // 4th Section Nodes
    @FXML private Label neighbourhood_cleansed;

    // 5th Section Nodes
    @FXML private Label neighbourhood_overview;

    // 6th Section Nodes
    @FXML private Label accommodates, bathrooms, bedrooms, beds, minimum_nights, maximum_nights, availability_365;
    @FXML private ListView<String> amenities;

    // 7th Section Nodes
    @FXML private Label numberOfReviews;
    @FXML private ProgressBar review_scores_cleanliness, review_scores_checkin, review_scores_communication, review_scores_location, review_scores_value;

    private static NewAirbnbListing listing;
    private static final Image DEFAULT_IMAGE = new Image("imagePlaceholderDark.jpg");

    /**
     * Adds the listing to Booking.
     */
    @FXML
    private void addToBooking() {
        if(MainController.getCurrentUser().getUsername() == null){
            new Alerts(Alert.AlertType.ERROR,"Error", null, "You need to have an account to be able to book a property :(");
        }else{
            // actually booking
            if(!BookingController.checkProperty(listing)){
                BookingController.addListing(listing);
                new Alerts(Alert.AlertType.INFORMATION,"Success", null, "Listing with listing id: " + listing.getId() + " has been added to your bookings, you could either continue browsing or go to the booking page to confirm booking");
            }
        }
    }

    /**
     * Opens a GoogleMaps link in the default browser of the location of the current property.
     * Credits to OlivierGrenoble https://stackoverflow.com/questions/27879854/desktop-getdesktop-browse-hangs
     */
    @FXML
    private void openGoogleMaps(){
        double latitude = listing.getLatitude();
        double longitude = listing.getLongitude();

        URI uri;
        try {
            uri = new URI("https://www.google.com/maps/place/" + latitude + "," + longitude);
            if (SystemUtils.IS_OS_LINUX) {
                // Workaround for Linux because "Desktop.getDesktop().browse()" doesn't work on some Linux implementations
                if (Runtime.getRuntime().exec(new String[] { "which", "xdg-open" }).getInputStream().read() != -1) {
                    Runtime.getRuntime().exec(new String[] { "xdg-open", uri.toString() });
                } else {
                    new Alerts(Alert.AlertType.ERROR,"Error", null, "xdg-open not supported! Can't open this on linux!");
                }
            } else {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(uri);
                } else {
                    new Alerts(Alert.AlertType.ERROR,"Browse URL", null, "Desktop command not supported!");
                }
            }

        } catch (IOException | URISyntaxException e) {
            new Alerts(Alert.AlertType.ERROR,"Browse URL", null, "Failed to open URL");
        }
    }

    /**
     * Initialises the FXML component.
     *
     * @param location FXML placeholder location.
     * @param resources FXML placeholder resources.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root.setVisible(false);
    }

    /**
     * Assigns a new listing and reassigns values.
     *
     * @param newListing Listing you want to set the values of.
     */
    public void setListing(NewAirbnbListing newListing){
        listing = newListing;
        assign();
        root.setVisible(true);
    }

    /**
     * Assigns all the values to the panel.
     */
    private void assign() {
        // 1st Section Nodes
        int ratingValue = listing.getReviewScoresRating();
        if (ratingValue == -1){rating.setText("?");}
        else{rating.setText(ratingValue + "");}
        Color reviewColour = ListingBox.calculateRatingColour(listing.getReviewScoresRating());
        rating.setBackground(new Background(new BackgroundFill(reviewColour, new CornerRadii(15), Insets.EMPTY)));
        rating.setPadding(new Insets(10, 20, 10, 20));

        id.setText(listing.getId());
        name.setText(listing.getName());

        // 2nd Section Nodes
        picture_url_loading.setVisible(true);
        bindImageProperties(DEFAULT_IMAGE);
        new Thread(() -> {
            Image urlImage = new Image(listing.getPictureURL().toString(), false);
            picture_url_loading.setVisible(false);
            if (!urlImage.isError()){
                bindImageProperties(urlImage);
            }
        }).start();

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(picture_url.fitWidthProperty());
        clip.heightProperty().bind(picture_url.fitHeightProperty());
        clip.setArcHeight(50);
        clip.setArcWidth(50);
        picture_url.setClip(clip);

        // 3rd Section Nodes
        Rectangle hostClip = new Rectangle();
        hostClip.widthProperty().bind(host_picture_url.fitWidthProperty());
        hostClip.heightProperty().bind(host_picture_url.fitHeightProperty());
        hostClip.setArcHeight(70);
        hostClip.setArcWidth(70);
        host_picture_url.setClip(hostClip);

        host_picture_url_loading.setVisible(true);
        host_picture_url.setImage(DEFAULT_IMAGE);
        new Thread(() -> {
            Image urlImage = new Image(listing.getHostPictureURL().toString(), false);
            host_picture_url_loading.setVisible(false);
            if (!urlImage.isError()){
                host_picture_url.setImage(urlImage);
            }
        }).start();

        host_id.setText(listing.getHostID());
        host_name.setText(listing.getHostName());
        price.setText("$" + listing.getPrice() + "0");

        // 4th Section Nodes
        neighbourhood_cleansed.setText(listing.getNeighbourhoodCleansed());

        // 5th Section Nodes
        String overview = listing.getNeighbourhoodOverview();
        if (overview.equals("")){neighbourhood_overview.setText("EMPTY DESCRIPTION");}
        else {neighbourhood_overview.setText(overview);}

        // 6th Section Nodes
        accommodates.setText(listing.getAccommodates() + "");
        bathrooms.setText(listing.getBathrooms() + "");
        bedrooms.setText(listing.getBedrooms() + "");
        beds.setText(listing.getBeds() + "");
        minimum_nights.setText(listing.getMinimumNights() + "");
        maximum_nights.setText(listing.getMaximumNights() + "");
        availability_365.setText(listing.getAvailability365() + "/365");
        amenities.getItems().clear();
        if(listing.getAmenities() != null) {
            amenities.getItems().addAll(listing.getAmenities());
        }

        // 7th Section Nodes
        int reviewCount = listing.getNumberOfReviews();
        if (reviewCount == 1){numberOfReviews.setText(reviewCount + " Review");}
        else{numberOfReviews.setText(reviewCount + " Reviews");}

        double cleanliness = ((double)listing.getReviewScoresCleanliness())/10;
        review_scores_cleanliness.setProgress(cleanliness);
        review_scores_cleanliness.setId(calcReviewID(cleanliness));

        double checkin = ((double)listing.getReviewScoresCheckin())/10;
        review_scores_checkin.setProgress(checkin);
        review_scores_checkin.setId(calcReviewID(checkin));

        double communication = ((double)listing.getReviewScoresCommunication())/10;
        review_scores_communication.setProgress(communication);
        review_scores_communication.setId(calcReviewID(communication));

        double location = ((double)listing.getReviewScoresLocation())/10;
        review_scores_location.setProgress(location);
        review_scores_location.setId(calcReviewID(location));

        double value = ((double)listing.getReviewScoresValue())/10;
        review_scores_value.setProgress(value);
        review_scores_value.setId(calcReviewID(value));
    }

    /**
     * Binds the size of the Image to the ImageView for the picture_url.
     *
     * @param newImage The image to bind to picture_url.
     */
    private void bindImageProperties(Image newImage) {
        double newConstant = newImage.getHeight()/newImage.getWidth();

        picture_url.setImage(newImage);
        property_type.setText(listing.getPropertyType());

        image_holder.maxHeightProperty().bind(image_holder.minHeightProperty());
        image_holder.maxWidthProperty().bind(image_holder.minWidthProperty());

        image_holder.minWidthProperty().bind(root.widthProperty().subtract(50));
        image_holder.minHeightProperty().bind(image_holder.minWidthProperty().multiply(newConstant));

        picture_url.fitWidthProperty().bind(image_holder.widthProperty());
        picture_url.fitHeightProperty().bind(image_holder.heightProperty());
    }

    /**
     * Calculates what the colour of the progress bar should be and returns the CSS ID of the colour.
     *
     * @param review The progress bar value. Max 1.0.
     * @return The ID of the colour.
     */
    private String calcReviewID(double review){
        if (review == 1){
            return "green";
        }
        else if (review >= 0.6){
            return "orange";
        }
        else{
            return "red";
        }
    }
}
