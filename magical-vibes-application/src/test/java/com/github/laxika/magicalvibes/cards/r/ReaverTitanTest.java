package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ReaverTitan.class)
class ReaverTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Protection applies to sources with mana value 3 or less")
    void protectionFromManaValueAtMostThree() {
        Permanent titan = addReaverTitan();
        Card manaValueThree = sourceCard("Low-cost source", "{3}");
        Card manaValueFour = sourceCard("High-cost source", "{4}");

        assertThat(gqs.hasProtectionFromSource(gd, titan, manaValueThree)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, titan, manaValueFour)).isFalse();
    }

    @Test
    @DisplayName("Crew 4 animates Reaver Titan and its attack trigger damages each opponent")
    void crewAndAttackTrigger() {
        harness.setLife(player2, 20);
        addReaverTitan();
        addCreatureReady(player1, sourceCreature("Crew", 4, 4, "{3}{W}"));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent titan = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(gqs.isCreature(gd, titan)).isTrue();
        assertThat(titan.isAnimatedUntilEndOfTurn()).isTrue();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(5);
    }

    private Permanent addReaverTitan() {
        return addCreatureReady(player1, new ReaverTitan());
    }

    private static Card sourceCreature(String name, int power, int toughness, String manaCost) {
        Card card = sourceCard(name, manaCost);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card sourceCard(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost(manaCost);
        return card;
    }
}
