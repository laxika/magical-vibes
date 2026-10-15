package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PunkFrogs.class, Shock.class, ProdigalPyromancer.class})
class PunkFrogsTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when its controller does not pay")
    void wardCountersUnpaidSpell() {
        Permanent frogs = harness.addToBattlefieldAndReturn(player1, new PunkFrogs());
        castOpponentShock(frogs, 1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Punk Frogs");
    }

    @Test
    @DisplayName("Paying ward lets an opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent frogs = harness.addToBattlefieldAndReturn(player1, new PunkFrogs());
        castOpponentShock(frogs, 3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(frogs);
    }

    @Test
    @DisplayName("Paying exactly three mana allows the opposing spell to deal damage")
    void payingWardAllowsSpellDamage() {
        Permanent frogs = harness.addToBattlefieldAndReturn(player1, new PunkFrogs());
        castOpponentShock(frogs, 3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(frogs.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Declining an affordable ward payment prevents the spell's damage")
    void decliningWardPreventsDamage() {
        Permanent frogs = harness.addToBattlefieldAndReturn(player1, new PunkFrogs());
        castOpponentShock(frogs, 3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(frogs.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's own spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent frogs = harness.addToBattlefieldAndReturn(player1, new PunkFrogs());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, frogs.getId());

        harness.passBothPriorities();

        assertThat(frogs.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Ward counters an opposing activated ability without removing its source")
    void wardCountersOpposingAbility() {
        Permanent frogs = harness.addToBattlefieldAndReturn(player1, new PunkFrogs());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, null, frogs.getId());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(frogs.getMarkedDamage()).isZero();
        assertThat(pyromancer.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
        harness.assertNotInGraveyard(player2, "Prodigal Pyromancer");
    }

    @Test
    @DisplayName("Paying ward allows an opposing activated ability to deal damage")
    void payingWardAllowsAbilityDamage() {
        Permanent frogs = harness.addToBattlefieldAndReturn(player1, new PunkFrogs());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, null, frogs.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(frogs.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    private void castOpponentShock(Permanent target, int wardMana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, wardMana);
        harness.castInstant(player2, 0, target.getId());
    }
}
