package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErgRaidersTest extends BaseCardTest {

    private Permanent addErgRaiders(boolean summoningSick) {
        Permanent perm = new Permanent(new ErgRaiders());
        perm.setSummoningSick(summoningSick);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 2 damage to its controller at end step when it did not attack")
    void dealsTwoDamageWhenItDidNotAttack() {
        harness.setLife(player1, 20);
        Permanent erg = addErgRaiders(false);

        advanceToEndStep();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(erg.getId());

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when it attacked this turn")
    void doesNotTriggerWhenItAttackedThisTurn() {
        harness.setLife(player1, 20);
        Permanent erg = addErgRaiders(false);
        erg.setAttackedThisTurn(true);

        advanceToEndStep();

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not deal damage when it came under control this turn")
    void doesNotDamageWhenItCameUnderControlThisTurn() {
        harness.setLife(player1, 20);
        addErgRaiders(true);

        advanceToEndStep();

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
