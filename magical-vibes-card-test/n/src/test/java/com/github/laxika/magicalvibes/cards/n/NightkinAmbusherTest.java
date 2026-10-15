package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightkinAmbusher.class, GrizzlyBears.class, Shock.class, ProdigalPyromancer.class})
class NightkinAmbusherTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives four rad counters to the chosen player")
    void entersGivesFourRadCountersToTargetPlayer() {
        harness.enterBattlefieldAndReturn(player1, new NightkinAmbusher());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent ambusher = addCreatureReady(player1, new NightkinAmbusher());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, ambusher.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(ambusher.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can't be blocked while the defending player has a rad counter")
    void cantBeBlockedWhenDefenderHasRadCounters() {
        gd.playerRadCounters.put(player2.getId(), 1);
        Permanent ambusher = addReadyAttacker();
        addReadyBlocker();

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
        assertThat(ambusher.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked when the defending player has no rad counters")
    void canBeBlockedWhenDefenderHasNoRadCounters() {
        harness.setLife(player2, 20);
        addReadyAttacker();
        addReadyBlocker();

        beginBlockerDeclaration();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void entersCanTargetControllerAndAddsToExistingRadCounters() {
        gd.playerRadCounters.put(player1.getId(), 2);
        harness.enterBattlefieldAndReturn(player1, new NightkinAmbusher());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(6);
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void payingWardAllowsSpellToResolve() {
        Permanent ambusher = addCreatureReady(player1, new NightkinAmbusher());
        prepareOpponentShock(ambusher, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(ambusher.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void decliningWardCountersSpellEvenWithEnoughMana() {
        Permanent ambusher = addCreatureReady(player1, new NightkinAmbusher());
        prepareOpponentShock(ambusher, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(ambusher.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardDoesNotCounterControllersSpell() {
        Permanent ambusher = addCreatureReady(player1, new NightkinAmbusher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, ambusher.getId());

        assertThat(ambusher.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardCountersOpponentsActivatedAbility() {
        Permanent ambusher = addCreatureReady(player1, new NightkinAmbusher());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, ambusher.getId());
        resolveAllTriggers();

        assertThat(ambusher.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    @Test
    void controllersRadCountersDoNotPreventBlocking() {
        gd.playerRadCounters.put(player1.getId(), 4);
        addReadyAttacker();
        addReadyBlocker();
        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    void losingLastRadCounterBeforeBlocksAllowsBlocking() {
        gd.playerRadCounters.put(player2.getId(), 1);
        addReadyAttacker();
        addReadyBlocker();
        gd.playerRadCounters.put(player2.getId(), 0);
        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isBlocking()).isTrue();
    }

    private void prepareOpponentShock(Permanent ambusher, int wardMana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, wardMana);
        harness.castInstant(player2, 0, ambusher.getId());
    }

    private Permanent addReadyAttacker() {
        Permanent ambusher = addCreatureReady(player1, new NightkinAmbusher());
        ambusher.setAttacking(true);
        return ambusher;
    }

    private void addReadyBlocker() {
        addCreatureReady(player2, new GrizzlyBears());
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
