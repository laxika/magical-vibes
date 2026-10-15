package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcolyteOfTheInferno.class, GrizzlyBears.class, WallOfWood.class, FieryImpulse.class})
class AcolyteOfTheInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked deals 2 damage to the blocker")
    void becomingBlockedDamagesBlocker() {
        Permanent acolyte = addCreatureReady(player1, new AcolyteOfTheInferno());
        acolyte.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Becoming blocked by two creatures triggers once per blocker")
    void triggersOncePerBlocker() {
        Permanent acolyte = addCreatureReady(player1, new AcolyteOfTheInferno());
        acolyte.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggerCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Acolyte of the Inferno"))
                .count();
        assertThat(triggerCount).isEqualTo(2);

        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent acolyte = addCreatureReady(player1, new AcolyteOfTheInferno());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(acolyte.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("No renown when blocked, but the blocker still takes 2 damage")
    void noRenownWhenBlocked() {
        Permanent acolyte = addCreatureReady(player1, new AcolyteOfTheInferno());
        Permanent wall = addCreatureReady(player2, new WallOfWood());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(acolyte.isRenowned()).isFalse();
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A renowned creature does not gain another counter in a later combat")
    void renownOnlyOnce() {
        Permanent acolyte = addCreatureReady(player1, new AcolyteOfTheInferno());

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();
        assertThat(acolyte.isRenowned()).isTrue();
        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        acolyte.untap();
        acolyte.setAttacking(false);
        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Renown does not trigger if the creature is already renowned")
    void alreadyRenownedDoesNotTrigger() {
        Permanent acolyte = addCreatureReady(player1, new AcolyteOfTheInferno());
        acolyte.setRenowned(true);
        acolyte.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        acolyte.setAttacking(true);
        prepareDeclareBlockers();

        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Killing the blocker with the trigger does not make the attacker unblocked")
    void remainsBlockedAfterBlockerDies() {
        Permanent acolyte = addCreatureReady(player1, new AcolyteOfTheInferno());
        addCreatureReady(player2, new AcolyteOfTheInferno());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Acolyte of the Inferno");

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(acolyte.isRenowned()).isFalse();
        assertThat(acolyte.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The damage trigger resolves even after Acolyte leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        Permanent acolyte = addCreatureReady(player1, new AcolyteOfTheInferno());
        addCreatureReady(player2, new AcolyteOfTheInferno());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player2, List.of(new FieryImpulse()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, acolyte.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Acolyte of the Inferno");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Acolyte of the Inferno");
        harness.assertLife(player2, 20);
    }
}
