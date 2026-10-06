package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

    @Test
    @DisplayName("Ravenous draw and the attack requirement trigger separately")
    void ravenousDrawIsAnIndependentTrigger() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Ravener()));
        harness.setHand(player1, List.of(new Ravener()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantForX(player1, 0, 5, List.of(bear.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.assertInHand(player1, "Ravener");
    }

    @Test
    @DisplayName("Ravener can be cast during an opponent's upkeep")
    void flashAllowsCastingDuringOpponentsTurn() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        castRavener(1, bear.getId(), player2.getId());

        harness.assertOnBattlefield(player1, "Ravener");
        declareAttackers(player2, List.of());
        assertThat(bear.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A tapped target is not required to attack")
    void tappedTargetCannotAttack() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setTapped(true);

        castRavener(1, bear.getId(), player2.getId());

        declareAttackers(List.of());
        assertThat(bear.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick target is not required to attack")
    void summoningSickTargetCannotAttack() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setSummoningSick(true);

        castRavener(1, bear.getId(), player2.getId());

        declareAttackers(List.of());
        assertThat(bear.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("At X=0 Ravener dies but its attack trigger still resolves")
    void zeroCountersDoesNotPreventAttackTrigger() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        castRavener(0, bear.getId(), player2.getId());

        harness.assertNotOnBattlefield(player1, "Ravener");
        harness.assertInGraveyard(player1, "Ravener");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private void castRavener(int x, java.util.UUID creatureTarget, java.util.UUID playerTarget) {
        harness.setHand(player1, List.of(new Ravener()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);

        harness.castInstantForX(player1, 0, x, List.of(creatureTarget, playerTarget));
        resolveAllTriggers();
    }
}
