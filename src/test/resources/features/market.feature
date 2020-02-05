@market
  Feature: Marketing application test suite

    @market1
    Scenario: Required fields end-to-end
      Given I navigate to "quote" page
      When I fill out "required" fields
      And I submit the form
      Then I verify "required" fields

    @market2
    Scenario: Email field behavior
      Given I navigate to "quote" page
      When I fill out "email" field with "wrongformat"
      And I submit the form
      Then I verify error "Please enter a valid email address." displayed