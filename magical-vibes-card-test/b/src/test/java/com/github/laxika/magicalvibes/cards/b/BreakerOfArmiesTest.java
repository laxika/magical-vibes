package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakerOfArmies.class, CoralhelmGuide.class})
class BreakerOfArmiesTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block Breaker of Armies")
    void allAbleCreaturesMustBlock() {
        addCreatureReady(player1, new BreakerOfArmies());

        addCreatureReady(player2, new CoralhelmGuide());
        addCreatureReady(player2, new CoralhelmGuide());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Breaker of Armies")
    void tappedCreaturesNotForcedToBlock() {
        addCreatureReady(player1, new BreakerOfArmies());

        Permanent untapped = addCreatureReady(player2, new CoralhelmGuide());
        Permanent tapped = addCreatureReady(player2, new CoralhelmGuide());
        tapped.tap();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A blocker may choose between two attacking Breakers")
    void blockerMayChooseBetweenBreakers() {
        addCreatureReady(player1, new BreakerOfArmies());
        addCreatureReady(player1, new BreakerOfArmies());
        Permanent blocker = addCreatureReady(player2, new CoralhelmGuide());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness does not exempt a creature from blocking")
    void summoningSickCreatureMustBlock() {
        addCreatureReady(player1, new BreakerOfArmies());
        Permanent blocker = addCreatureReady(player2, new CoralhelmGuide());
        blocker.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A nonattacking Breaker does not force creatures to block")
    void nonattackingBreakerDoesNotRequireBlocks() {
        addCreatureReady(player1, new BreakerOfArmies());
        addCreatureReady(player1, new CoralhelmGuide());
        Permanent blocker = addCreatureReady(player2, new CoralhelmGuide());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }
}
