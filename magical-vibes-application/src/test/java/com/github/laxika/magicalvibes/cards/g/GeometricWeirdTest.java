package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeometricWeird.class, DarkRitual.class})
class GeometricWeirdTest extends BaseCardTest {

    @Test
    @DisplayName("Sets base power and toughness to the greatest distinct stack-source count")
    void setsBasePowerAndToughnessToGreatestDistinctStackSourceCount() {
        Permanent weird = harness.addToBattlefieldAndReturn(player1, new GeometricWeird());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        resolveEndStepTrigger(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, weird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, weird)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not change base power and toughness when the optional ability is declined")
    void doesNotChangeBasePowerAndToughnessWhenDeclined() {
        Permanent weird = harness.addToBattlefieldAndReturn(player1, new GeometricWeird());

        resolveEndStepTrigger(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, weird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, weird)).isEqualTo(1);
    }

    private void resolveEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
