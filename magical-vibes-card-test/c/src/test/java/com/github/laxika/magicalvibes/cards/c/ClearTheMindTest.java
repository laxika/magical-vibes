package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClearTheMind.class, GrizzlyBears.class, GiantSpider.class})
class ClearTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles the target graveyard and draws a card")
    void shufflesTargetGraveyardAndDrawsCard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GiantSpider()));
        harness.setHand(player1, List.of(new ClearTheMind()));
        harness.setLibrary(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        int targetLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(targetLibrarySize + 2);
        harness.assertInHand(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClearTheMind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Self-targeting replenishes an empty library before drawing")
    void selfTargetDrawsFromShuffledGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ClearTheMind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Clear the Mind");
    }

    @Test
    @DisplayName("An empty target graveyard still allows the caster to draw")
    void emptyTargetGraveyardStillDraws() {
        GiantSpider spider = new GiantSpider();
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(spider));
        harness.setHand(player1, List.of(new ClearTheMind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        int targetHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spider);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(targetHandSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Clear the Mind");
    }
}
