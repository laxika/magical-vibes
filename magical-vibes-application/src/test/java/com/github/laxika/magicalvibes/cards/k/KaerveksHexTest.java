package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KaerveksHexTest extends BaseCardTest {

    private static Card blackGreenCreature() {
        Card card = new Card();
        card.setName("Golgari Brute");
        card.setType(CardType.CREATURE);
        card.setManaCost("{B}{G}");
        card.setColor(CardColor.BLACK);
        card.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
        card.setPower(3);
        card.setToughness(3);
        return card;
    }

    private void castHex() {
        harness.setHand(player1, List.of(new KaerveksHex()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Green nonblack creatures take 2 damage")
    void greenCreaturesTakeTwoDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        Permanent spider = findPermanent(player2, "Giant Spider");

        castHex();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonblack, nongreen creatures take only 1 damage")
    void nonblackNongreenCreaturesTakeOneDamage() {
        harness.addToBattlefield(player2, new AngelOfMercy());
        Permanent angel = findPermanent(player2, "Angel of Mercy");

        castHex();

        assertThat(angel.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Black creatures are untouched")
    void blackCreaturesAreUntouched() {
        harness.addToBattlefield(player2, new BlackKnight());
        Permanent knight = findPermanent(player2, "Black Knight");

        castHex();

        assertThat(knight.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A black-green creature takes only the additional 1 damage")
    void blackGreenCreatureTakesOneDamage() {
        harness.addToBattlefield(player2, blackGreenCreature());
        Permanent brute = findPermanent(player2, "Golgari Brute");

        castHex();

        assertThat(brute.getMarkedDamage()).isEqualTo(1);
    }
}
