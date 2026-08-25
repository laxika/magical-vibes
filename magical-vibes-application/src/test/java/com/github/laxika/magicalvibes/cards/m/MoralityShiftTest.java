package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoralityShift.class, GrizzlyBears.class, GiantSpider.class})
class MoralityShiftTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges the controller's graveyard and library, then shuffles the new library")
    void exchangesGraveyardAndLibrary() {
        Card libraryCard = new GrizzlyBears();
        Card graveyardCard = new GiantSpider();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new MoralityShift()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCard);
        harness.assertInGraveyard(player1, "Morality Shift");
    }

    @Test
    @DisplayName("Only exchanges the controller's zones")
    void opponentZonesAreUntouched() {
        Card opponentLibraryCard = new GrizzlyBears();
        Card opponentGraveyardCard = new GiantSpider();
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new GiantSpider()));
        harness.setHand(player1, List.of(new MoralityShift()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
    }
}
