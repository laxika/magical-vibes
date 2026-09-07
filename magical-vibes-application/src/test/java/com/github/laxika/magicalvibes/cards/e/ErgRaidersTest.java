package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErgRaiders.class, NevinyrralsDisk.class})
class ErgRaidersTest extends BaseCardTest {

    private Permanent addErgRaiders(boolean summoningSick) {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ErgRaiders());
        perm.setSummoningSick(summoningSick);
        return perm;
    }

    private void advanceToEndStep() {
        advanceToEndStep(player1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
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
    @DisplayName("Triggers but does not deal damage when it came under control this turn")
    void triggersButDoesNotDamageWhenItCameUnderControlThisTurn() {
        harness.setLife(player1, 20);
        addErgRaiders(true);

        advanceToEndStep();

        assertThat(gd.stack).singleElement()
                .extracting(StackEntry::getEntryType)
                .isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers only during its controller's end step")
    void triggersOnlyDuringItsControllersEndStep() {
        harness.setLife(player1, 20);
        addErgRaiders(false);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Resolves its trigger after the source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player1, 20);
        Permanent erg = addErgRaiders(false);
        harness.addToBattlefield(player2, new NevinyrralsDisk());

        advanceToEndStep();

        assertThat(gd.stack).singleElement()
                .extracting(StackEntry::getSourcePermanentId)
                .isEqualTo(erg.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Erg Raiders");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
