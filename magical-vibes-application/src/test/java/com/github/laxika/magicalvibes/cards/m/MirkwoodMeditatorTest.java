package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirkwoodMeditator.class, Forest.class})
class MirkwoodMeditatorTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall may set Mirkwood Meditator's base power and toughness to 4/2")
    void landfallMaySetBasePowerAndToughness() {
        Permanent meditator = harness.addToBattlefieldAndReturn(player1, new MirkwoodMeditator());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(meditator.getEffectivePower()).isEqualTo(4);
        assertThat(meditator.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining landfall leaves Mirkwood Meditator unchanged")
    void decliningLandfallLeavesItUnchanged() {
        Permanent meditator = harness.addToBattlefieldAndReturn(player1, new MirkwoodMeditator());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(meditator.getEffectivePower()).isEqualTo(2);
        assertThat(meditator.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Landfall base power and toughness change wears off at end of turn")
    void landfallChangeWearsOffAtEndOfTurn() {
        Permanent meditator = harness.addToBattlefieldAndReturn(player1, new MirkwoodMeditator());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(meditator.getEffectivePower()).isEqualTo(2);
        assertThat(meditator.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Mirkwood Meditator")
    void opponentLandDoesNotTrigger() {
        Permanent meditator = harness.addToBattlefieldAndReturn(player1, new MirkwoodMeditator());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(meditator.getEffectivePower()).isEqualTo(2);
        assertThat(meditator.getEffectiveToughness()).isEqualTo(4);
    }
}
