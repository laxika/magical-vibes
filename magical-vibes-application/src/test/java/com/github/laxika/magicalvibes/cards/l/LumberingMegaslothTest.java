package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LumberingMegasloth.class})
class LumberingMegaslothTest extends BaseCardTest {

    @Test
    void costsFullAmountWithNoCounters() {
        harness.setHand(player1, java.util.List.of(new LumberingMegasloth()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void countsCountersOnPlayersAndPermanents() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new LumberingMegasloth());
        ownPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new LumberingMegasloth());
        opposingPermanent.setCounterCount(CounterType.CHARGE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerRadCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player1.getId(), 1);
        gd.playerSparkCounters.put(player2.getId(), 1);
        gd.playerExperienceCounters.put(player1.getId(), 1);

        harness.setHand(player1, java.util.List.of(new LumberingMegasloth()));
        // Eight counters reduce {10}{G}{G} to {2}{G}{G}.
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotCastWithInsufficientManaAfterReduction() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new LumberingMegasloth());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, java.util.List.of(new LumberingMegasloth()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void entersTapped() {
        harness.setHand(player1, java.util.List.of(new LumberingMegasloth()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sloth = findPermanent(player1, "Lumbering Megasloth");
        assertThat(sloth.isTapped()).isTrue();
    }

    @Test
    void excessCountersDoNotReduceColoredManaCost() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new LumberingMegasloth());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 12);
        harness.setHand(player1, java.util.List.of(new LumberingMegasloth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void entersTappedWithoutBeingCast() {
        Permanent sloth = harness.enterBattlefieldAndReturn(player1, new LumberingMegasloth());

        assertThat(sloth.isTapped()).isTrue();
    }

    @Test
    void tramplesOverBlocker() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        Permanent attacker = addCreatureReady(player1, new LumberingMegasloth());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent blocker = addCreatureReady(player2, new LumberingMegasloth());
        declareAttackersAndPrepareBlockers(java.util.List.of(0));
        gs.declareBlockers(gd, player2, java.util.List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, java.util.Map.of(
                blocker.getId(), 8, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Lumbering Megasloth");
        harness.assertOnBattlefield(player1, "Lumbering Megasloth");
    }
}
