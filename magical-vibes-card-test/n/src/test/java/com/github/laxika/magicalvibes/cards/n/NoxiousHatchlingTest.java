package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoxiousHatchling.class, DoomBlade.class, GrizzlyBears.class, Shock.class})
class NoxiousHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with four -1/-1 counters (6/6 becomes 2/2)")
    void entersWithFourMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NoxiousHatchling()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent hatchling = findHatchling(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(hatchling.getEffectivePower()).isEqualTo(2);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(2);
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
    @DisplayName("Casting a green spell removes a -1/-1 counter")
    void castingGreenSpellRemovesCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a non-black, non-green spell does not remove a counter")
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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(6);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLACK", "GREEN"})
    @DisplayName("A black-green hybrid spell triggers both abilities regardless of mana paid")
    void hybridSpellRemovesTwoCounters(ManaColor manaPaid) {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NoxiousHatchling()));
        harness.addMana(player1, manaPaid, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent newcomer = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(newcomer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both hybrid-spell triggers resolve safely with only one counter remaining")
    void hybridSpellWithOneCounterRemaining() {
        Permanent hatchling = addReadyHatchling(player1);
        hatchling.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        castHybridHatchling(player1, ManaColor.BLACK);

        resolveAllTriggers();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("An opponent's black-green spell does not remove counters")
    void opponentSpellDoesNotRemoveCounters() {
        Permanent hatchling = addReadyHatchling(player1);
        castHybridHatchling(player2, ManaColor.GREEN);

        resolveAllTriggers();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Wither deals combat damage to creatures as -1/-1 counters")
    void witherDamageAddsCountersInsteadOfMarkedDamage() {
        Permanent attacker = addReadyHatchling(player1);
        Permanent blocker = addCreatureReady(player2, new NoxiousHatchling());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Wither deals normal life loss to an unblocked opponent")
    void witherDamageToPlayerIsNormalDamage() {
        addReadyHatchling(player1);
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    private Permanent addReadyHatchling(Player player) {
        Permanent perm = addCreatureReady(player, new NoxiousHatchling());
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        return perm;
    }

    private void castHybridHatchling(Player player, ManaColor manaPaid) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new NoxiousHatchling()));
        harness.addMana(player, manaPaid, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreature(player, 0);
    }

    private Permanent findHatchling(Player player) {
        return findPermanent(player, "Noxious Hatchling");
    }
}
