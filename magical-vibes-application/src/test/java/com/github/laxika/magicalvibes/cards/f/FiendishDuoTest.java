package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiendishDuo.class, Blaze.class, GrizzlyBears.class, TurnToFrog.class})
class FiendishDuoTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles damage dealt to an opponent")
    void doublesDamageToOpponent() {
        harness.addToBattlefield(player1, new FiendishDuo());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Doubles damage from an opponent's source")
    void doublesDamageFromOpponentsSource() {
        harness.addToBattlefield(player1, new FiendishDuo());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, 3, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Does not double damage dealt to its controller")
    void doesNotDoubleDamageToController() {
        harness.addToBattlefield(player1, new FiendishDuo());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not double damage dealt to an opponent's permanent")
    void doesNotDoubleDamageToOpponentsPermanent() {
        harness.addToBattlefield(player1, new FiendishDuo());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1, bear.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two copies quadruple damage to their opponent")
    void twoCopiesQuadrupleDamage() {
        harness.addToBattlefield(player1, new FiendishDuo());
        harness.addToBattlefield(player1, new FiendishDuo());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Doubles its own unblocked combat damage")
    void doublesItsOwnCombatDamage() {
        addCreatureReady(player1, new FiendishDuo());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Stops doubling damage when it loses all abilities")
    void stopsDoublingAfterLosingAbilities() {
        Permanent duo = harness.addToBattlefieldAndReturn(player1, new FiendishDuo());
        harness.setHand(player1, List.of(new TurnToFrog(), new Blaze()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, duo.getId());
        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
    }
}
