package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.d.DeepAnalysis;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuietSpeculation.class, AncientGrudge.class, DeepAnalysis.class, GrizzlyBears.class, ThinkTwice.class})
class QuietSpeculationTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only cards with flashback from the target player's library")
    void offersOnlyFlashbackCards() {
        Card thinkTwice = new ThinkTwice();
        Card ancientGrudge = new AncientGrudge();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(thinkTwice, bears, ancientGrudge));

        castQuietSpeculation(player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).containsExactly(thinkTwice, ancientGrudge);
    }

    @Test
    @DisplayName("Puts up to three chosen flashback cards into the target player's graveyard")
    void putsUpToThreeFlashbackCardsIntoTargetGraveyard() {
        Card thinkTwice = new ThinkTwice();
        Card ancientGrudge = new AncientGrudge();
        Card deepAnalysis = new DeepAnalysis();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(thinkTwice, ancientGrudge, deepAnalysis, bears));

        castQuietSpeculation(player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(thinkTwice, ancientGrudge, deepAnalysis);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(thinkTwice, ancientGrudge, deepAnalysis);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Can decline after choosing fewer than three cards")
    void canChooseFewerThanThreeCards() {
        Card thinkTwice = new ThinkTwice();
        Card ancientGrudge = new AncientGrudge();
        harness.setLibrary(player2, List.of(thinkTwice, ancientGrudge));

        castQuietSpeculation(player2.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(thinkTwice);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(ancientGrudge);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Resolves without a search when the target library has no flashback cards")
    void noMatchingCardsSkipsSearch() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));

        castQuietSpeculation(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Quiet Speculation");
    }

    private void castQuietSpeculation(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new QuietSpeculation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
