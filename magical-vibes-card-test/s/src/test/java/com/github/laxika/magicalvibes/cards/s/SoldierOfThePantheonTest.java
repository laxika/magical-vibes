package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FleecemaneLion;
import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.d.DestructiveRevelry;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoldierOfThePantheon.class, FleecemaneLion.class, TravelingPhilosopher.class, BronzeSable.class,
        DestructiveRevelry.class, Humility.class, MycosynthLattice.class})
class SoldierOfThePantheonTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from multicolored sources")
    void hasProtectionFromMulticoloredSources() {
        Permanent soldier = addCreatureReady(player1, new SoldierOfThePantheon());
        Permanent multicoloredSource = addCreatureReady(player2, new FleecemaneLion());
        Permanent monocoloredSource = addCreatureReady(player2, new TravelingPhilosopher());

        assertThat(gqs.hasProtectionFromSource(gd, soldier, multicoloredSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, soldier, monocoloredSource)).isFalse();
    }

    @Test
    @DisplayName("Gains 1 life when an opponent casts a multicolored spell")
    void gainsLifeWhenOpponentCastsMulticoloredSpell() {
        addCreatureReady(player1, new SoldierOfThePantheon());
        prepareMainPhase(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player2, new FleecemaneLion(), "{G}{W}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's monocolored spell")
    void doesNotTriggerForOpponentMonocoloredSpell() {
        addCreatureReady(player1, new SoldierOfThePantheon());
        prepareMainPhase(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player2, new TravelingPhilosopher(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger for your own multicolored spell")
    void doesNotTriggerForOwnMulticoloredSpell() {
        addCreatureReady(player1, new SoldierOfThePantheon());
        prepareMainPhase(player1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, new FleecemaneLion(), "{G}{W}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void multicoloredCreatureCannotBlock() {
        Permanent soldier = addCreatureReady(player1, new SoldierOfThePantheon());
        soldier.setAttacking(true);
        addCreatureReady(player2, new FleecemaneLion());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void monocoloredCreatureCanBlock() {
        Permanent soldier = addCreatureReady(player1, new SoldierOfThePantheon());
        soldier.setAttacking(true);
        addCreatureReady(player2, new TravelingPhilosopher());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(soldier);
    }

    @Test
    void preventsCombatDamageFromMulticoloredCreature() {
        addCreatureReady(player1, new FleecemaneLion()).setAttacking(true);
        Permanent soldier = addCreatureReady(player2, new SoldierOfThePantheon());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(soldier);
        assertThat(soldier.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void doesNotTriggerForColorlessSpell() {
        addCreatureReady(player1, new SoldierOfThePantheon());
        prepareMainPhase(player2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new BronzeSable(), "{2}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void eachSoldierTriggersIndependently() {
        addCreatureReady(player1, new SoldierOfThePantheon());
        addCreatureReady(player1, new SoldierOfThePantheon());
        prepareMainPhase(player2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new FleecemaneLion(), "{G}{W}");

        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void triggerResolvesAfterSoldierLeavesBattlefield() {
        Permanent soldier = addCreatureReady(player1, new SoldierOfThePantheon());
        prepareMainPhase(player2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player2, new FleecemaneLion(), "{G}{W}");
        gd.playerBattlefields.get(player1.getId()).remove(soldier);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void humilityRemovesProtectionFromMulticoloredBlockers() {
        Permanent soldier = addCreatureReady(player1, new SoldierOfThePantheon());
        soldier.setAttacking(true);
        addCreatureReady(player2, new FleecemaneLion());
        harness.addToBattlefield(player1, new Humility());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(soldier);
    }

    @Test
    void doesNotTriggerForSpellMadeColorlessByLattice() {
        addCreatureReady(player1, new SoldierOfThePantheon());
        harness.addToBattlefield(player1, new MycosynthLattice());
        prepareMainPhase(player2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new FleecemaneLion(), "{G}{W}");

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void canBeTargetedBySpellMadeColorlessByLattice() {
        Permanent soldier = addCreatureReady(player1, new SoldierOfThePantheon());
        harness.addToBattlefield(player1, new MycosynthLattice());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new DestructiveRevelry()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, soldier.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(soldier);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

}
