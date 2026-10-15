package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abolish;
import com.github.laxika.magicalvibes.cards.r.RibCageSpider;
import com.github.laxika.magicalvibes.model.Card;
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

    @Test
    @DisplayName("Uses only your graveyard and puts the spell there after resolution")
    void leavesOpponentsGraveyardUntouched() {
        Card opposingCreature = new RibCageSpider();
        Card opposingNoncreature = new Abolish();
        harness.setGraveyard(player2, List.of(opposingCreature, opposingNoncreature));

        castWithGraveyardCards(new RibCageSpider());

        harness.assertOnBattlefield(player1, "Rib Cage Spider");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(opposingCreature, opposingNoncreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Search for Survivors");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castWithGraveyardCards(Card... graveyardCards) {
        harness.setGraveyard(player1, List.of(graveyardCards));
        harness.castFromHand(player1, new SearchForSurvivors(), "{2}{R}");
        harness.passBothPriorities();
    }
}
