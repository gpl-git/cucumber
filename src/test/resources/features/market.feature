@market @regression
  Feature: Marketing application test suite

    @market1 @smoke
    Scenario: Required fields end-to-end
      Given I navigate to "quote" page
      When I fill out "required" fields
      And I submit the form
      Then I verify "required" fields

    @market2 @acceptance
    Scenario: Email field behavior
      Given I navigate to "quote" page
      When I fill out "email" field with "wrongformat"
      And I submit the form
      Then I verify error "Please enter a valid email address." displayed

    @market3 @acceptance
    Scenario: Email field behavior
      Given I navigate to "quote" page
      When I fill out "password" field with "welcome1"
      When I fill out "confirmPassword" field with "welcome2"
      And I submit the form
      Then I verify error "Passwords do not match!" displayed