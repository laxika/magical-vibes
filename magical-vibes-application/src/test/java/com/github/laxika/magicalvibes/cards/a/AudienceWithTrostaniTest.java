package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AudienceWithTrostani.class})
class AudienceWithTrostaniTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Plant and draws for distinct controlled creature token names")
    void createsPlantAndDrawsForDistinctControlledCreatureTokenNames() {
        harness.setHand(player1, List.of(new AudienceWithTrostani()));
        harness.addToBattlefield(player1, creatureToken("Bear"));
        harness.addToBattlefield(player1, creatureToken("Bear"));
        harness.addToBattlefield(player1, creatureToken("Spirit"));
        harness.addToBattlefield(player1, nontokenCreature("Bear"));
        harness.addToBattlefield(player1, artifactToken("Clue"));
        harness.addToBattlefield(player2, creatureToken("Opponent"));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castAudienceWithTrostani();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(findPermanents(player1, "Plant")).singleElement()
                .satisfies(plant -> {
                    assertThat(plant.getEffectivePower()).isZero();
                    assertThat(plant.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Counts the Plant token when it is the only creature token")
    void countsCreatedPlantToken() {
        harness.setHand(player1, List.of(new AudienceWithTrostani()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castAudienceWithTrostani();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(findPermanents(player1, "Plant")).hasSize(1);
    }

    private void castAudienceWithTrostani() {
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setManaCost("");
        card.setType(CardType.CREATURE);
        card.setToken(true);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private Card nontokenCreature(String name) {
        Card card = creatureToken(name);
        card.setToken(false);
        return card;
    }

    private Card artifactToken(String name) {
        Card card = creatureToken(name);
        card.setType(CardType.ARTIFACT);
        return card;
    }
}
