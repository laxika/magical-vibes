package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VoroshTheHunter.class)
class VoroshTheHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{G} puts six +1/+1 counters on Vorosh")
    void payingManaPutsCountersOnVorosh() {
        Permanent vorosh = addAttackingVorosh();

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vorosh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Declining the payment puts no counters on Vorosh")
    void decliningPaymentPutsNoCountersOnVorosh() {
        Permanent vorosh = addAttackingVorosh();

        resolveCombatToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vorosh.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addAttackingVorosh() {
        Permanent vorosh = addCreatureReady(player1, new VoroshTheHunter());
        vorosh.setAttacking(true);
        return vorosh;
    }

    private void resolveCombatToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
