package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeOfVecna.class, HillGiantHerdgorger.class})
class EyeOfVecnaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card and causes its controller to lose 2 life")
    void enteringDrawsAndLosesLife() {
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new EyeOfVecna(), "{2}");
        harness.passBothPriorities();

        int handSizeBeforeEtb = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeEtb + 1);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Paying {2} during upkeep draws a card and causes its controller to lose 2 life")
    void payingUpkeepCostDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new EyeOfVecna());
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        int handSizeBeforeDraw = gd.playerHands.get(player1.getId()).size();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeDraw + 1);
        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the upkeep cost does not draw or cause life loss")
    void decliningUpkeepCostDoesNothing() {
        harness.addToBattlefield(player1, new EyeOfVecna());
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        int handSizeBeforeDraw = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeDraw);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Eye of Vecna does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new EyeOfVecna());
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));
        harness.setLife(player1, 20);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The second player's upkeep payment accepts colored mana and affects only that player")
    void secondControllerCanPayWithColoredMana() {
        harness.addToBattlefield(player2, new EyeOfVecna());
        harness.setLibrary(player2, List.of(new HillGiantHerdgorger()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize);
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Having less than 2 life does not prevent paying the upkeep mana cost")
    void upkeepLifeLossIsNotAnAdditionalPaymentCost() {
        harness.addToBattlefield(player1, new EyeOfVecna());
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));
        harness.setLife(player1, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertLife(player1, -1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
