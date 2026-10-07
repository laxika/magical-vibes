package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
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

@CardUsed({SplatterThug.class, DrudgeBeetle.class})
class SplatterThugTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castSplatterThug(true);

        Permanent thug = findPermanent(player1, "Splatter Thug");
        assertThat(thug.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, thug)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thug)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter")
    void decliningLeavesNoCounter() {
        castSplatterThug(false);

        assertThat(findPermanent(player1, "Splatter Thug").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unleashed Splatter Thug can't block")
    void unleashedCantBlock() {
        Permanent thug = addCreatureReady(player1, new SplatterThug());
        thug.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without a +1/+1 counter it blocks normally")
    void blocksWithoutCounter() {
        addCreatureReady(player1, new SplatterThug());
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Splatter Thug").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An unleashed Splatter Thug can still attack")
    void unleashedCanStillAttack() {
        harness.setLife(player2, 20);
        Permanent thug = addCreatureReady(player1, new SplatterThug());
        thug.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Removing the unleash counter allows blocking again")
    void blocksAfterUnleashCounterIsRemoved() {
        castSplatterThug(true);
        Permanent thug = findPermanent(player1, "Splatter Thug");
        thug.setSummoningSick(false);
        thug.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(thug.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A counter added after declining unleash still prevents blocking")
    void laterCounterPreventsBlocking() {
        castSplatterThug(false);
        Permanent thug = findPermanent(player1, "Splatter Thug");
        thug.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First strike kills a blocking 2/2 before it can deal damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        harness.setLife(player2, 20);
        Permanent thug = addCreatureReady(player1, new SplatterThug());
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Splatter Thug");
        harness.assertInGraveyard(player2, "Drudge Beetle");
        assertThat(thug.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    private void castSplatterThug(boolean unleash) {
        harness.setHand(player1, List.of(new SplatterThug()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
