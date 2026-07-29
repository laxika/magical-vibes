package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RadiantEssenceTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    @DisplayName("Base 2/3 when no opponent controls a black permanent")
    void baseWithoutBlackPermanent() {
        harness.addToBattlefield(player1, new RadiantEssence());

        Permanent essence = findPermanent(player1, "Radiant Essence");
        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+2 (3/5) when an opponent controls a black permanent")
    void boostWhenOpponentControlsBlackPermanent() {
        harness.addToBattlefield(player1, new RadiantEssence());
        harness.addToBattlefield(player2, createCreature("Bog Rats", 1, 1, CardColor.BLACK));

        Permanent essence = findPermanent(player1, "Radiant Essence");
        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(5);
    }

    @Test
    @DisplayName("No boost when the opponent's permanent is not black")
    void noBoostWhenPermanentNotBlack() {
        harness.addToBattlefield(player1, new RadiantEssence());
        harness.addToBattlefield(player2, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));

        Permanent essence = findPermanent(player1, "Radiant Essence");
        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(3);
    }

    @Test
    @DisplayName("The controller's own black permanent does not grant the boost")
    void noBoostFromOwnBlackPermanent() {
        harness.addToBattlefield(player1, new RadiantEssence());
        harness.addToBattlefield(player1, createCreature("Bog Rats", 1, 1, CardColor.BLACK));

        Permanent essence = findPermanent(player1, "Radiant Essence");
        assertThat(gqs.getEffectivePower(gd, essence)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, essence)).isEqualTo(3);
    }
}
