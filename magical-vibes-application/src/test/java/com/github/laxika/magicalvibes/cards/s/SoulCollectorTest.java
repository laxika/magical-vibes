package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoastWatcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulCollector.class, CoastWatcher.class, SparkSpray.class, ShorelineRanger.class})
class SoulCollectorTest extends BaseCardTest {

    @Test
    void returnsCreatureItDamagedToTheBattlefieldUnderItsControl() {
        destroyCoastWatcherInCombat();

        harness.assertOnBattlefield(player1, "Coast Watcher");
        harness.assertNotInGraveyard(player2, "Coast Watcher");
    }

    @Test
    void doesNotReturnCreatureItDidNotDamage() {
        harness.addToBattlefield(player1, new SoulCollector());
        harness.addToBattlefield(player2, new CoastWatcher());

        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Coast Watcher"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Coast Watcher");
        harness.assertNotOnBattlefield(player1, "Coast Watcher");
    }

    @Test
    void returnsCreatureDamagedEarlierInTheTurnWhenItDiesLater() {
        Permanent soulCollector = addCreatureReady(player1, new SoulCollector());
        Permanent shorelineRanger = addCreatureReady(player2, new ShorelineRanger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, shorelineRanger.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soul Collector");
        harness.assertOnBattlefield(player1, "Shoreline Ranger");
        harness.assertNotInGraveyard(player2, "Shoreline Ranger");
        assertThat(soulCollector.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new SoulCollector()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent soulCollector = findPermanent(player1, "Soul Collector");
        assertThat(soulCollector.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(soulCollector));
        harness.passBothPriorities();

        assertThat(soulCollector.isFaceDown()).isFalse();
    }

    private void destroyCoastWatcherInCombat() {
        addCreatureReady(player1, new SoulCollector());
        addCreatureReady(player2, new CoastWatcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.passBothPriorities();
    }
}
