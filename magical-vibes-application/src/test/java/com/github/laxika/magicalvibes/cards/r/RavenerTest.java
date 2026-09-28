package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ravener.class, GrizzlyBears.class})
class RavenerTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous enters with X counters and draws at X=5")
    void ravenousAtFiveAndForcesAttack() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        castRavener(5, bear.getId(), player2.getId());

        Permanent ravener = findPermanent(player1, "Ravener");
        assertThat(ravener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInHand(player1, "Grizzly Bears");

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        declareAttackers(List.of(0));
        assertThat(bear.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Ravenous does not draw below X=5")
    void ravenousBelowFiveDoesNotDraw() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        castRavener(4, bear.getId(), player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Ravener")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ravener can target only an opponent")
    void targetMustBeOpponent() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ravener()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(bear.getId(), player1.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRavener(int x, java.util.UUID creatureTarget, java.util.UUID playerTarget) {
        harness.setHand(player1, List.of(new Ravener()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);

        gs.playCard(gd, player1, 0, x, null, null,
                List.of(creatureTarget, playerTarget), List.of());
        resolveAllTriggers();
    }
}
