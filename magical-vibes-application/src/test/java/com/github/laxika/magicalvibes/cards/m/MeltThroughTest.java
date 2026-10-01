package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({MeltThrough.class, ColossalDreadmaw.class})
class MeltThroughTest extends BaseCardTest {

    @Test
    @DisplayName("deals 2 damage to a creature and preserves that damage through cleanup")
    void damagesCreatureAndPreservesDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new MeltThrough()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("can target a planeswalker")
    void canTargetPlaneswalker() {
        Card planeswalkerCard = new Card();
        planeswalkerCard.setName("Test Planeswalker");
        planeswalkerCard.setType(CardType.PLANESWALKER);
        planeswalkerCard.setManaCost("{3}");
        planeswalkerCard.setLoyalty(5);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, planeswalkerCard);
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new MeltThrough()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("can target a player")
    void canTargetPlayer() {
        harness.setHand(player1, List.of(new MeltThrough()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }
}
