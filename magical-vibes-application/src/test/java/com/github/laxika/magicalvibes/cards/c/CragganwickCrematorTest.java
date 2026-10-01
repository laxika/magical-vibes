package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.g.GrimLavamancer;
import com.github.laxika.magicalvibes.cards.j.JaceTheMindSculptor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CragganwickCremator.class, GrimLavamancer.class, Brainstorm.class, JaceTheMindSculptor.class})
class CragganwickCrematorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB: discarding a creature card deals its power to target player")
    void discardsCreatureDealsPowerToPlayer() {
        // Only one card left in hand after casting, so the random discard is deterministic.
        harness.setHand(player1, List.of(new CragganwickCremator(), new GrimLavamancer()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, List.of(player2.getId()));

        // Resolve creature spell → enters battlefield, ETB triggers.
        harness.passBothPriorities();
        // Resolve ETB triggered ability → discard Grim Lavamancer (1/1), deal 1 to player2.
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grim Lavamancer");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("ETB: damage can target its controller")
    void discardsCreatureCanDamageController() {
        harness.setHand(player1, List.of(new CragganwickCremator(), new GrimLavamancer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, List.of(player1.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("ETB: damage can target a planeswalker")
    void discardsCreatureDealsPowerToPlaneswalker() {
        var jace = harness.addToBattlefieldAndReturn(player2, new JaceTheMindSculptor());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new CragganwickCremator(), new GrimLavamancer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, List.of(jace.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grim Lavamancer");
    }

    @Test
    @DisplayName("ETB: discarding a noncreature card deals no damage")
    void discardsNoncreatureDealsNoDamage() {
        harness.setHand(player1, List.of(new CragganwickCremator(), new Brainstorm()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, List.of(player2.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Brainstorm is discarded (non-creature) — the ability has no further effect.
        harness.assertInGraveyard(player1, "Brainstorm");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB: still discards a card even with no legal creature to fuel damage")
    void discardsEvenWhenNoDamage() {
        harness.setHand(player1, List.of(new CragganwickCremator(), new Brainstorm()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Hand is emptied by the random discard.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB: with an empty hand, nothing is discarded and no damage is dealt")
    void doesNothingWhenHandIsEmpty() {
        harness.setHand(player1, List.of(new CragganwickCremator()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
