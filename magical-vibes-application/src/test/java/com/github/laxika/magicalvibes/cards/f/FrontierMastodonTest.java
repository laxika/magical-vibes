package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FrontierMastodonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when you control a creature with power 4 or greater")
    void entersWithCounterForFerocious() {
        addCreature(player1, "Large Creature", 4, 4);

        Permanent mastodon = castMastodon();

        assertThat(mastodon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not enter with a counter when your creatures have less than 4 power")
    void doesNotEnterWithCounterBelowThreshold() {
        addCreature(player1, "Small Creature", 3, 3);

        Permanent mastodon = castMastodon();

        assertThat(mastodon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not count an opponent's creature")
    void doesNotCountOpponentsCreature() {
        addCreature(player2, "Opponents Large Creature", 4, 4);

        Permanent mastodon = castMastodon();

        assertThat(mastodon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castMastodon() {
        harness.setHand(player1, List.of(new FrontierMastodon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Frontier Mastodon");
    }

    private void addCreature(Player player, String name, int power, int toughness) {
        harness.addToBattlefield(player, makeCreature(name, power, toughness));
    }

    private Card makeCreature(String name, int power, int toughness) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
