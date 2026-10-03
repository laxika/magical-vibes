package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatteringKrasis.class, GrizzlyBears.class})
class BatteringKrasisTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Battering Krasis when a tougher creature enters")
    void evolvesForTougherCreature() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new BatteringKrasis());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger when neither stat is greater")
    void doesNotEvolveForEqualCreature() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new BatteringKrasis());

        harness.setHand(player1, List.of(new BatteringKrasis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void evolvesWhenOnlyPowerIsGreater() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new BatteringKrasis());
        krasis.setToughnessModifier(2);
        harness.setHand(player1, List.of(new BatteringKrasis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        krasis.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotEvolveForOpponentsCreature() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new BatteringKrasis());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void rechecksStatsWhenTriggerResolves() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new BatteringKrasis());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gd.stack).hasSize(1);
        krasis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesEnteringCreaturesLastKnownPowerAfterItDies() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new BatteringKrasis());
        krasis.setPowerModifier(-1);
        krasis.setToughnessModifier(2);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gd.stack).hasSize(1);

        entering.setPowerModifier(-1);
        entering.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(entering);
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new BatteringKrasis());
        Permanent blocker = addCreatureReady(player2, new BatteringKrasis());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }
}
