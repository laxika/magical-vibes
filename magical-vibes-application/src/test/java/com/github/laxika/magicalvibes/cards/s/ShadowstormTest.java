package com.github.laxika.magicalvibes.cards.s;

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

class ShadowstormTest extends BaseCardTest {

    /** A 2/2 creature with shadow for test purposes. */
    private static Card shadowCreature() {
        Card card = new Card();
        card.setName("Dauthi Slayer");
        card.setType(CardType.CREATURE);
        card.setManaCost("{B}{B}");
        card.setColor(CardColor.BLACK);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.SHADOW));
        return card;
    }

    /** A 3/3 creature with shadow that survives 2 damage. */
    private static Card toughShadowCreature() {
        Card card = shadowCreature();
        card.setName("Dauthi Marauder");
        card.setPower(3);
        card.setToughness(3);
        return card;
    }

    @Test
    @DisplayName("Deals 2 damage to every creature with shadow, regardless of controller")
    void damagesAllShadowCreatures() {
        harness.addToBattlefield(player1, shadowCreature());
        harness.addToBattlefield(player2, shadowCreature());

        harness.setHand(player1, List.of(new Shadowstorm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dauthi Slayer");
        harness.assertNotOnBattlefield(player2, "Dauthi Slayer");
    }

    @Test
    @DisplayName("Creatures without shadow are untouched")
    void doesNotDamageNonShadowCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shadowstorm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A shadow creature with toughness greater than 2 survives")
    void toughShadowCreatureSurvives() {
        harness.addToBattlefield(player2, toughShadowCreature());

        harness.setHand(player1, List.of(new Shadowstorm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dauthi Marauder");
    }

    @Test
    @DisplayName("Players take no damage")
    void doesNotDamagePlayers() {
        harness.setHand(player1, List.of(new Shadowstorm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
