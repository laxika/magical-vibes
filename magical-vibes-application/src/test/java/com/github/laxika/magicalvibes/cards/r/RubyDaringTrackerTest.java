package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RubyDaringTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a boost without a creature with power 4 or greater")
    void doesNotBoostWithoutLargeCreature() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ruby.getPowerModifier()).isZero();
        assertThat(ruby.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Gets +2/+2 when it attacks while you control a creature with power 4 or greater")
    void boostsWithLargeCreature() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());
        addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ruby.getPowerModifier()).isEqualTo(2);
        assertThat(ruby.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(ruby.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        Permanent ruby = addCreatureReady(player1, new RubyDaringTracker());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(ruby.isTapped()).isTrue();
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
