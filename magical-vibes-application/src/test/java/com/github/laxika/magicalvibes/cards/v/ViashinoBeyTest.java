package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.Brainwash;
import com.github.laxika.magicalvibes.cards.e.Errantry;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViashinoBey.class, GiantCockroach.class, Brainwash.class, Errantry.class})
class ViashinoBeyTest extends BaseCardTest {

    @Test
    @DisplayName("Viashino Bey does not force its controller's creatures to attack when it stays back")
    void ownCreaturesAreNotForcedWhenBeyStaysBack() {
        Permanent bey = addCreatureReady(player1, new ViashinoBey());
        Permanent cockroach = addCreatureReady(player1, new GiantCockroach());

        declareAttackers(List.of());

        assertThat(bey.isAttacking()).isFalse();
        assertThat(cockroach.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("When Viashino Bey attacks, all other able creatures its controller controls must attack")
    void ownCreaturesMustAttackWhenBeyAttacks() {
        addCreatureReady(player1, new ViashinoBey());
        addCreatureReady(player1, new GiantCockroach());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Viashino Bey does not force an opponent's creatures to attack")
    void opponentsCreaturesAreNotForced() {
        harness.addToBattlefield(player1, new ViashinoBey());
        Permanent cockroach = addCreatureReady(player2, new GiantCockroach());

        declareAttackers(player2, List.of());

        assertThat(cockroach.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Viashino Bey and all able creatures can attack together")
    void allAbleCreaturesCanAttack() {
        addCreatureReady(player1, new ViashinoBey());
        addCreatureReady(player1, new GiantCockroach());

        declareAttackers(List.of(0, 1));

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Tapped and summoning-sick creatures need not attack with Viashino Bey")
    void unableCreaturesCanStayBack() {
        addCreatureReady(player1, new ViashinoBey());
        Permanent tapped = addCreatureReady(player1, new GiantCockroach());
        tapped.tap();
        harness.addToBattlefield(player1, new GiantCockroach());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Viashino Bey does not require payment of another creature's attack cost")
    void optionalAttackCostCanBeDeclined() {
        addCreatureReady(player1, new ViashinoBey());
        Permanent taxed = addCreatureReady(player1, new GiantCockroach());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Brainwash());
        aura.setAttachedTo(taxed.getId());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Viashino Bey can attack alone when Errantry prevents it attacking with others")
    void attackAloneRestrictionTakesPrecedence() {
        Permanent bey = addCreatureReady(player1, new ViashinoBey());
        addCreatureReady(player1, new GiantCockroach());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Errantry());
        aura.setAttachedTo(bey.getId());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 13);
    }
}
