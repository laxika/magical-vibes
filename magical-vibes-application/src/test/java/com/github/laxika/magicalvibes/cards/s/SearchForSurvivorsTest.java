package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abolish;
import com.github.laxika.magicalvibes.cards.r.RibCageSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearchForSurvivors.class, RibCageSpider.class, Abolish.class})
class SearchForSurvivorsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the randomly selected creature card to the battlefield")
    void returnsCreatureToBattlefield() {
        Card creature = new RibCageSpider();
        castWithGraveyardCards(creature);

        harness.assertOnBattlefield(player1, "Rib Cage Spider");
        harness.assertNotInGraveyard(player1, "Rib Cage Spider");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Exiles the randomly selected noncreature card")
    void exilesNoncreature() {
        Card noncreature = new Abolish();
        castWithGraveyardCards(noncreature);

        harness.assertNotInGraveyard(player1, "Abolish");
        harness.assertNotOnBattlefield(player1, "Abolish");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(noncreature);
    }

    @Test
    @DisplayName("Does nothing when the graveyard is empty")
    void emptyGraveyard() {
        castWithGraveyardCards();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Moves exactly one random card and leaves the other graveyard card")
    void movesExactlyOneRandomCard() {
        Card creature = new RibCageSpider();
        Card noncreature = new Abolish();
        castWithGraveyardCards(creature, noncreature);

        boolean creatureReturned = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        boolean noncreatureExiled = gd.getPlayerExiledCards(player1.getId()).contains(noncreature);

        assertThat(creatureReturned).isNotEqualTo(noncreatureExiled);
        Card remainingCard = creatureReturned ? noncreature : creature;
        Card movedCard = creatureReturned ? creature : noncreature;
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(remainingCard.getId()))
                .noneMatch(card -> card.getId().equals(movedCard.getId()));
    }

    private void castWithGraveyardCards(Card... graveyardCards) {
        harness.setGraveyard(player1, List.of(graveyardCards));
        harness.setHand(player1, List.of(new SearchForSurvivors()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
