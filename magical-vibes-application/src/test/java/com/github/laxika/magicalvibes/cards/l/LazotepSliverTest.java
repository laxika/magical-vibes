package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirrorEntity;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpinedSliver;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LazotepSliver.class, SpinedSliver.class, GrizzlyBears.class, Shock.class, MirrorEntity.class})
class LazotepSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Slivers you control gain afflict 2")
    void grantsAfflictToOwnSlivers() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LazotepSliver());
        addCreatureReady(player1, new SpinedSliver());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        declareBlockers(0, 1);

        harness.passBothPriorities(); // Resolve Spined Sliver's trigger.
        harness.passBothPriorities(); // Resolve Lazotep Sliver's afflict trigger.

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A nontoken Sliver dying amasses Slivers 2")
    void amassesSliversWhenOwnNontokenSliverDies() {
        addCreatureReady(player1, new LazotepSliver());
        Permanent sliver = addCreatureReady(player1, new SpinedSliver());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, sliver.getId());
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Sliver Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.SLIVER, CardSubtype.ARMY);
        assertThat(army.getEffectivePower()).isEqualTo(2);
        assertThat(army.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-Sliver dying does not amass")
    void doesNotAmassWhenNonSliverDies() {
        addCreatureReady(player1, new LazotepSliver());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(findPermanents(player1, "Sliver Army")).isEmpty();
    }

    @Test
    @DisplayName("Lazotep Sliver's own death also amasses Slivers 2")
    void selfDeathAmassesSlivers() {
        Permanent lazotep = addCreatureReady(player1, new LazotepSliver());
        lazotep.setMarkedDamage(4);

        harness.runStateBasedActions();
        resolveAllTriggers();

        Permanent army = findPermanent(player1, "Sliver Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void tokenCopyDoesNotAmassWhenItDies() {
        LazotepSliver token = new LazotepSliver();
        token.setToken(true);
        Permanent copy = addCreatureReady(player1, token);
        copy.setMarkedDamage(4);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sliver Army")).isEmpty();
    }

    @Test
    void tokenArmyDeathDoesNotAmass() {
        addCreatureReady(player1, new LazotepSliver());
        Permanent sliver = addCreatureReady(player1, new SpinedSliver());
        sliver.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        Permanent army = findPermanent(player1, "Sliver Army");

        army.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sliver Army")).isEmpty();
    }

    @Test
    void opponentSliverDeathDoesNotAmass() {
        addCreatureReady(player1, new LazotepSliver());
        Permanent sliver = addCreatureReady(player2, new SpinedSliver());
        sliver.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sliver Army")).isEmpty();
        assertThat(findPermanents(player2, "Sliver Army")).isEmpty();
    }

    @Test
    void repeatedDeathsGrowTheExistingArmy() {
        addCreatureReady(player1, new LazotepSliver());
        Permanent first = addCreatureReady(player1, new SpinedSliver());
        first.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        Permanent army = findPermanent(player1, "Sliver Army");
        Permanent second = addCreatureReady(player1, new SpinedSliver());

        second.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sliver Army")).containsExactly(army);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void simultaneousSelfAndAllyDeathsBothAmass() {
        Permanent lazotep = addCreatureReady(player1, new LazotepSliver());
        Permanent sliver = addCreatureReady(player1, new SpinedSliver());
        lazotep.setMarkedDamage(4);
        sliver.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sliver Army")).hasSize(1);
        assertThat(findPermanent(player1, "Sliver Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @CardUsed({LazotepSliver.class, MirrorEntity.class, GrizzlyBears.class, Shock.class})
    void creatureThatGainedSliverTypeAmassesWhenItDies() {
        addCreatureReady(player1, new LazotepSliver());
        addCreatureReady(player1, new MirrorEntity());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 1, 2, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sliver Army")).hasSize(1);
        assertThat(findPermanent(player1, "Sliver Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void selfAfflictTriggersOnceForMultipleBlockers() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LazotepSliver());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void multipleLazotepSliversGrantSeparateAfflictInstances() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LazotepSliver());
        addCreatureReady(player1, new LazotepSliver());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
    }

    @Test
    void nonSliverDoesNotGainAfflict() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LazotepSliver());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    void opponentSliverDoesNotGainAfflict() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new LazotepSliver());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new SpinedSliver());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    private void declareBlockers(int blockerIndex, int attackerIndex) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
