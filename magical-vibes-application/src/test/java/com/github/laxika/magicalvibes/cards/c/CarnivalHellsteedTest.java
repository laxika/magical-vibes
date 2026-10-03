package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeadbridgeGoliath;
import com.github.laxika.magicalvibes.cards.e.ExperimentOne;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CarnivalHellsteed.class, DeadbridgeGoliath.class, ExperimentOne.class})
class CarnivalHellsteedTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castCarnivalHellsteed(true);

        Permanent hellsteed = findPermanent(player1, "Carnival Hellsteed");
        assertThat(hellsteed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, hellsteed)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hellsteed)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter")
    void decliningLeavesNoCounter() {
        castCarnivalHellsteed(false);

        assertThat(findPermanent(player1, "Carnival Hellsteed").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unleashed Carnival Hellsteed can't block")
    void unleashedCantBlock() {
        Permanent hellsteed = addCreatureReady(player1, new CarnivalHellsteed());
        hellsteed.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new DeadbridgeGoliath());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without a +1/+1 counter it blocks normally")
    void blocksWithoutCounter() {
        addCreatureReady(player1, new CarnivalHellsteed());
        addCreatureReady(player2, new DeadbridgeGoliath());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Carnival Hellsteed").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction is block-only — an unleashed Carnival Hellsteed can still attack")
    void unleashedCanStillAttack() {
        harness.setLife(player2, 20);
        Permanent hellsteed = addCreatureReady(player1, new CarnivalHellsteed());
        hellsteed.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Haste lets it attack on the turn it is cast")
    void attacksOnTurnItEnters() {
        harness.setLife(player2, 20);
        castCarnivalHellsteed(true);

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("First strike kills a 5/5 blocker before it deals damage")
    void killsBlockerBeforeRegularCombatDamage() {
        Permanent hellsteed = addCreatureReady(player1, new CarnivalHellsteed());
        addCreatureReady(player2, new DeadbridgeGoliath());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Carnival Hellsteed");
        harness.assertInGraveyard(player2, "Deadbridge Goliath");
        assertThat(hellsteed.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Removing the last +1/+1 counter lets an unleashed creature block")
    void blocksAfterLastCounterRemoved() {
        castCarnivalHellsteed(true);
        Permanent hellsteed = findPermanent(player1, "Carnival Hellsteed");
        hellsteed.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addCreatureReady(player2, new DeadbridgeGoliath());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(hellsteed.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Evolve sees the counter chosen for unleash as the creature enters")
    void unleashCounterCountsForEvolveTrigger() {
        Permanent experiment = addCreatureReady(player1, new ExperimentOne());
        experiment.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        castCarnivalHellsteed(true);
        resolveAllTriggers();

        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    private void castCarnivalHellsteed(boolean unleash) {
        harness.setHand(player1, List.of(new CarnivalHellsteed()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
