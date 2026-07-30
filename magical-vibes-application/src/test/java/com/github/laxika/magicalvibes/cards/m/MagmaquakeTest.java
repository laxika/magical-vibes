package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MagmaquakeTest extends BaseCardTest {

    /** A 2/2 flying creature for test purposes. */
    private static Card flyingCreature() {
        Card card = new Card();
        card.setName("Wind Drake");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}{U}");
        card.setColor(CardColor.BLUE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.FLYING));
        return card;
    }

    @Test
    @DisplayName("Deals X damage to each creature without flying")
    void killsNonFlyingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not damage creatures with flying")
    void doesNotDamageFlyers() {
        harness.addToBattlefield(player2, flyingCreature());
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstantForX(player1, 0, 4, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Deals X damage to each planeswalker")
    void damagesPlaneswalkers() {
        Permanent chandra = new Permanent(new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(chandra);
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstantForX(player1, 0, 6, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
    }

    @Test
    @DisplayName("Deals no damage to players")
    void doesNotDamagePlayers() {
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstantForX(player1, 0, 5, List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("X=0 leaves creatures alive")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Magmaquake()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
