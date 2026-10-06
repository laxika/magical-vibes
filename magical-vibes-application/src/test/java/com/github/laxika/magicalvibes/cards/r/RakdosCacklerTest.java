package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RakdosCackler.class, DrudgeBeetle.class})
class RakdosCacklerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castRakdosCackler(true);

        Permanent cackler = findPermanent(player1, "Rakdos Cackler");
        assertThat(cackler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, cackler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cackler)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter")
    void decliningLeavesNoCounter() {
        castRakdosCackler(false);

        Permanent cackler = findPermanent(player1, "Rakdos Cackler");
        assertThat(cackler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unleashed Rakdos Cackler can't block")
    void unleashedCantBlock() {
        Permanent cackler = addCreatureReady(player1, new RakdosCackler());
        cackler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without a +1/+1 counter it blocks normally")
    void blocksWithoutCounter() {
        addCreatureReady(player1, new RakdosCackler());
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Rakdos Cackler").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction is block-only — an unleashed Rakdos Cackler can still attack")
    void unleashedCanStillAttack() {
        harness.setLife(player2, 20);
        Permanent cackler = addCreatureReady(player1, new RakdosCackler());
        cackler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Removing the unleash counter allows it to block again")
    void blocksAfterUnleashCounterIsRemoved() {
        castRakdosCackler(true);
        Permanent cackler = findPermanent(player1, "Rakdos Cackler");
        cackler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(cackler.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A counter added after declining unleash still prevents blocking")
    void counterAddedAfterDecliningPreventsBlocking() {
        castRakdosCackler(false);
        Permanent cackler = findPermanent(player1, "Rakdos Cackler");
        cackler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new DrudgeBeetle());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRakdosCackler(boolean unleash) {
        harness.setHand(player1, List.of(new RakdosCackler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Rakdos Cackler");
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
