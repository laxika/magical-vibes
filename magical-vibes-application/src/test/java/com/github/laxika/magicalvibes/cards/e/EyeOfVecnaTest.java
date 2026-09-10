package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeOfVecna.class, GrizzlyBears.class})
class EyeOfVecnaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card and causes its controller to lose 2 life")
    void enteringDrawsAndLosesLife() {
        harness.setHand(player1, List.of(new EyeOfVecna()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
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
}
