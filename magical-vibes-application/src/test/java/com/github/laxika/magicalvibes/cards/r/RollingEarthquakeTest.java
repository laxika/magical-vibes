package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RollingEarthquakeTest extends BaseCardTest {

    /** A 2/2 creature with horsemanship for test purposes. */
    private static Card horsemanshipCreature() {
        Card card = new Card();
        card.setName("Wu Scout");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}{U}");
        card.setColor(CardColor.BLUE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.HORSEMANSHIP));
        return card;
    }

    @Test
    @DisplayName("Deals X damage to creatures without horsemanship")
    void damagesNonHorsemanshipCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RollingEarthquake()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 2);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Grizzly Bears (2/2) takes 2 damage and dies.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not damage creatures with horsemanship")
    void doesNotDamageHorsemanshipCreatures() {
        harness.addToBattlefield(player2, horsemanshipCreature());

        harness.setHand(player1, List.of(new RollingEarthquake()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 2);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Horsemanship creature survives.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wu Scout"));
    }

    @Test
    @DisplayName("Deals X damage to each player")
    void damagesEachPlayer() {
        harness.setHand(player1, List.of(new RollingEarthquake()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 3);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RollingEarthquake()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
