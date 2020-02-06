@market @regression
  Feature: Marketing application test suite

    @market1 @smoke
    Scenario: Required fields end-to-end
      Given I navigate to "quote" page
      When I fill out "required" fields
      When I click on "3rd party agreement" button
      And I accept alert
      And I submit the form
      Then I verify "required" fields

    @market2 @acceptance
    Scenario: Empty form error verification
      Given I navigate to "quote" page
      And I submit the form
      Then I verify error "This field is required." displayed

    @market3 @acceptance
    Scenario: Email field behavior
      Given I navigate to "quote" page
      When I fill out "email" field with "wrongformat"
      And I submit the form
      Then I verify error "Please enter a valid email address." displayed

    @market4 @acceptance
    Scenario: Password fields behavior
      Given I navigate to "quote" page
      When I fill out "password" field with "welcome1"
      When I fill out "confirmPassword" field with "welcome2"
      And I submit the form
      Then I verify error "Passwords do not match!" displayed

    @market5 @acceptance
    Scenario: 3rd party agreement
      Given I navigate to "quote" page
      When I click on "3rd party agreement" button
      Then I verify alert text as "Do you accept third party agreement?"
      And I accept alert

    @market6 @regression
    Scenario: Optional field - country of origin
      Given I navigate to "quote" page
      When I fill out "required" fields
      When I click on "3rd party agreement" button
      And I accept alert
      And I fill out optional fields
      And I submit the form
      Then I verify "required" fields