package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NipGwyllion;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoraciousHatchling.class, EliteVanguard.class, DoomBlade.class, GrizzlyBears.class,
        Shock.class, NipGwyllion.class, RayOfCommand.class, Cloudshift.class})
class VoraciousHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with four -1/-1 counters (6/6 becomes 2/2)")
    void entersWithFourMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VoraciousHatchling()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent hatchling = findHatchling(player1);
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(hatchling.getEffectivePower()).isEqualTo(2);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a white spell removes a -1/-1 counter")
    void castingWhiteSpellRemovesCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a black spell removes a -1/-1 counter")
    void castingBlackSpellRemovesCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bearsId);
        resolveAllTriggers();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a non-white, non-black spell does not remove a counter")
    void castingOtherColorSpellDoesNotRemoveCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Shock (no trigger)

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counter removal is clamped at zero when no counters remain")
    void counterRemovalClampedAtZero() {
        Permanent hatchling = addReadyHatchling(player1);
        hatchling.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("A white and black spell triggers both abilities independently for each controlled Hatchling")
    void dualColorSpellRemovesTwoCountersFromEachHatchling() {
        Permanent first = addReadyHatchling(player1);
        Permanent second = addReadyHatchling(player1);
        Permanent opposing = addReadyHatchling(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NipGwyllion()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(5);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(opposing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's white and black spell does not trigger Hatchling")
    void opponentSpellDoesNotRemoveCounters() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new NipGwyllion()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Lifelink gains life equal to combat damage after counter reductions")
    void lifelinkUsesReducedCombatDamage() {
        addReadyHatchling(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A pending trigger still removes a counter when Hatchling changes controllers")
    void pendingTriggerFollowsSourceAfterControlChange() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NipGwyllion()));
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hatchling.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hatchling);
        resolveAllTriggers();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Pending triggers cannot remove counters from Hatchling after it leaves and returns")
    void pendingTriggersDoNotAffectReturnedHatchling() {
        Permanent original = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NipGwyllion(), new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, original.getId());
        harness.passBothPriorities(); // Cloudshift's cast trigger affects the original permanent.
        harness.passBothPriorities(); // Cloudshift returns a new permanent with four counters.
        Permanent returned = findHatchling(player1);
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        resolveAllTriggers();

        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    private Permanent addReadyHatchling(Player player) {
        Permanent perm = addCreatureReady(player, new VoraciousHatchling());
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        return perm;
    }

    private Permanent findHatchling(Player player) {
        return findPermanent(player, "Voracious Hatchling");
    }
}
