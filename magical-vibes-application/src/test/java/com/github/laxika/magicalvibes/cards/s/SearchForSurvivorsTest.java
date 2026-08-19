package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchForSurvivorsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the randomly selected creature card to the battlefield")
    void returnsCreatureToBattlefield() {
        Card creature = new GrizzlyBears();
        castWithGraveyardCard(creature);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Exiles the randomly selected noncreature card")
    void exilesNoncreature() {
        Card noncreature = new HolyDay();
        castWithGraveyardCard(noncreature);

        harness.assertNotInGraveyard(player1, "Holy Day");
        harness.assertNotOnBattlefield(player1, "Holy Day");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(noncreature);
    }

    @Test
    @DisplayName("Does nothing when the graveyard is empty")
    void emptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        castWithGraveyardCard(null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void castWithGraveyardCard(Card graveyardCard) {
        harness.setGraveyard(player1, graveyardCard == null ? List.of() : List.of(graveyardCard));
        harness.setHand(player1, List.of(new SearchForSurvivors()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
