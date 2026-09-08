package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.SoldierOfFortune;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Undergrowth.class, SoldierOfFortune.class, StormCrow.class})
class UndergrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Unkicked, prevents all combat damage this turn")
    void unkickedPreventsAllCombatDamage() {
        castUndergrowth(false);

        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(gd.combatDamageExemptPredicate).isNull();
        harness.assertInGraveyard(player1, "Undergrowth");
    }

    @Test
    @DisplayName("Kicked, red creatures still deal combat damage")
    void kickedExemptsRedCreatures() {
        addCreatureReady(player1, new SoldierOfFortune());
        Permanent soldier = findPermanent(player1, "Soldier of Fortune");

        castUndergrowth(true);

        assertThat(gd.combatDamageExemptPredicate).isNotNull();
        assertThat(gqs.isPreventedFromDealingDamage(gd, soldier, true)).isFalse();
    }

    @Test
    @DisplayName("Kicked, nonred creatures are still prevented from dealing combat damage")
    void kickedStillPreventsNonredCreatures() {
        addCreatureReady(player1, new StormCrow());
        Permanent crow = findPermanent(player1, "Storm Crow");

        castUndergrowth(true);

        assertThat(gqs.isPreventedFromDealingDamage(gd, crow, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, crow, false)).isFalse();
    }

    @Test
    @DisplayName("Unkicked, prevents combat damage during combat")
    void unkickedPreventsCombatDamageDuringCombat() {
        addCreatureReady(player1, new SoldierOfFortune());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castUndergrowth(false);
        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
    @Test
    void unkickedPreventsCombatDamageToCreatures() {
        Permanent attacker = addCreatureReady(player1, new SoldierOfFortune());
        Permanent blocker = addCreatureReady(player2, new SoldierOfFortune());

        castUndergrowth(false);
        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Kicked, red combat damage gets through while nonred combat damage does not")
    void kickedOnlyAllowsRedCombatDamage() {
        addCreatureReady(player1, new SoldierOfFortune());
        addCreatureReady(player1, new StormCrow());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castUndergrowth(true);
        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Combat damage prevention ends at cleanup")
    void combatDamagePreventionEndsAtCleanup() {
        castUndergrowth(false);
        assertThat(gd.preventAllCombatDamage).isTrue();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isFalse();
        assertThat(gd.combatDamageExemptPredicate).isNull();
    }

    private void castUndergrowth(boolean kicked) {
        harness.setHand(player1, List.of(new Undergrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        if (kicked) {
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.castKickedInstant(player1, 0);
        } else {
            harness.castInstant(player1, 0);
        }
        harness.passBothPriorities();
    }
}
