package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OmnathLocusOfAll.class)
class OmnathLocusOfAllTest extends BaseCardTest {

    @Test
    @DisplayName("The controller's unspent mana becomes black instead of draining")
    void unspentManaBecomesBlackInsteadOfDraining() {
        addOmnath();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("An ineligible top card goes to hand without a reveal")
    void putsIneligibleTopCardIntoHand() {
        Card topCard = testCard("{3}");
        harness.setLibrary(player1, List.of(topCard));
        addOmnath();

        resolveOmnathTrigger();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The controller may decline revealing an eligible top card")
    void mayDeclineRevealingEligibleTopCard() {
        Card topCard = testCard("{W}{U}{B}");
        harness.setLibrary(player1, List.of(topCard));
        addOmnath();

        resolveOmnathTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Revealing an eligible top card adds three mana in its colors")
    void revealsEligibleTopCardAndAddsManaInItsColors() {
        Card topCard = testCard("{U}{R}{G}");
        harness.setLibrary(player1, List.of(topCard));
        addOmnath();

        resolveOmnathTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void addOmnath() {
        harness.addToBattlefield(player1, new OmnathLocusOfAll());
    }

    private void resolveOmnathTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card testCard(String manaCost) {
        Card card = new Card();
        card.setName("Test card");
        card.setManaCost(manaCost);
        return card;
    }
}
