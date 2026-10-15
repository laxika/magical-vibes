package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrontierMastodon.class, FeralKrushok.class})
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

    @Test
    @DisplayName("Enters without a counter on an empty battlefield")
    void entersWithoutCounterOnEmptyBattlefield() {
        Permanent mastodon = castMastodon();

        assertThat(mastodon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts power added by counters on an existing creature")
    void countsEffectivePowerFromCounters() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new FrontierMastodon());
        existing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent mastodon = castMastodon();

        assertThat(mastodon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple qualifying creatures still give only one counter")
    void multipleQualifyingCreaturesGiveOneCounter() {
        harness.addToBattlefield(player1, new FeralKrushok());
        harness.addToBattlefield(player1, new FeralKrushok());

        Permanent mastodon = castMastodon();

        assertThat(mastodon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A high printed power does not qualify when counters reduce it below four")
    void checksReducedEffectivePower() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new FeralKrushok());
        existing.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        Permanent mastodon = castMastodon();

        assertThat(mastodon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Entry without casting applies the counter immediately without a trigger")
    void noncastEntryAppliesCounterImmediately() {
        harness.addToBattlefield(player1, new FeralKrushok());

        Permanent mastodon = harness.enterBattlefieldAndReturn(player1, new FrontierMastodon());

        assertThat(mastodon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castMastodon() {
        harness.castFromHand(player1, new FrontierMastodon(), "{2}{G}");
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
