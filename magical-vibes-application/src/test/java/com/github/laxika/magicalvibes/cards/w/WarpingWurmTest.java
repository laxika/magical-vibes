package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarpingWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Printed phasing phases the Wurm out during its controller's untap step")
    void phasesOutOnUntap() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new WarpingWurm());

        advanceTurn();
        advanceTurn(); // player1's untap step

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(wurm);
    }

    @Test
    @DisplayName("Phasing back in puts a +1/+1 counter on the Wurm")
    void phasesInWithCounter() {
        Permanent wurm = phasedOutWurm();

        advanceTurn();
        advanceTurn(); // player1's untap step phases it back in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);

        harness.passBothPriorities(); // resolve the phase-in trigger and the upkeep trigger

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining to pay {2}{G}{U} at upkeep phases the Wurm out")
    void declinePhasesOut() {
        Permanent wurm = phasedOutWurm();

        advanceTurn();
        advanceTurn();
        resolveUntilPayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wurm);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(wurm);
    }

    @Test
    @DisplayName("Paying {2}{G}{U} keeps the Wurm on the battlefield and spends the mana")
    void payKeepsItOnBattlefield() {
        Permanent wurm = phasedOutWurm();

        advanceTurn();
        advanceTurn();
        resolveUntilPayPrompt();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    /**
     * Returns a Wurm that has already phased out through the untap-step turn-based action, so the
     * next time its controller untaps it phases back in.
     */
    private Permanent phasedOutWurm() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new WarpingWurm());
        advanceTurn();
        advanceTurn(); // player1's untap step — printed phasing phases it out
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(wurm);
        return wurm;
    }

    private void resolveUntilPayPrompt() {
        for (int i = 0; i < 3 && !(gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice); i++) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
