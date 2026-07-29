package com.github.laxika.magicalvibes.cards.t;

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

class TropicalStormTest extends BaseCardTest {

    private static Card creature(String name, String manaCost, CardColor color, int power, int toughness, Set<Keyword> keywords) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setKeywords(keywords);
        return card;
    }

    private static Card blueFlyer() {
        return creature("Wind Drake", "{2}{U}", CardColor.BLUE, 3, 3, Set.of(Keyword.FLYING));
    }

    private static Card redFlyer() {
        return creature("Goblin Balloon Brigade", "{1}{R}", CardColor.RED, 2, 2, Set.of(Keyword.FLYING));
    }

    private static Card blueGroundCreature() {
        return creature("Coral Merfolk", "{1}{U}", CardColor.BLUE, 1, 1, Set.of());
    }

    private static Card greenGroundCreature() {
        return creature("Forest Bear", "{1}{G}", CardColor.GREEN, 2, 2, Set.of());
    }

    private void castStorm(int x) {
        harness.setHand(player1, List.of(new TropicalStorm()));
        harness.addMana(player1, ManaColor.GREEN, x + 1);
        harness.castSorcery(player1, 0, x);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals X damage to non-blue flyers")
    void killsNonBlueFlyer() {
        harness.addToBattlefield(player2, redFlyer());

        castStorm(2);

        harness.assertNotOnBattlefield(player2, "Goblin Balloon Brigade");
    }

    @Test
    @DisplayName("Deals only 1 damage to blue creatures without flying")
    void blueGroundCreatureTakesOneDamage() {
        harness.addToBattlefield(player2, blueGroundCreature());
        harness.addToBattlefield(player2, greenGroundCreature());

        castStorm(3);

        // The 1/1 blue creature dies to the extra 1 damage; the green ground creature is untouched
        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
        harness.assertOnBattlefield(player2, "Forest Bear");
    }

    @Test
    @DisplayName("Blue flyers take X plus 1 damage")
    void blueFlyerTakesXPlusOne() {
        harness.addToBattlefield(player2, blueFlyer());

        castStorm(2);

        // 3/3 blue flyer: 2 (flying) + 1 (blue) = 3 damage
        harness.assertNotOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Blue flyer survives when X plus 1 is less than its toughness")
    void blueFlyerSurvivesLesserDamage() {
        harness.addToBattlefield(player2, blueFlyer());

        castStorm(1);

        harness.assertOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Deals no damage to players")
    void dealsNoDamageToPlayers() {
        castStorm(5);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With X=0 only blue creatures take damage")
    void xZeroOnlyDamagesBlueCreatures() {
        harness.addToBattlefield(player2, blueGroundCreature());
        harness.addToBattlefield(player2, redFlyer());

        castStorm(0);

        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
        harness.assertOnBattlefield(player2, "Goblin Balloon Brigade");
    }
}
