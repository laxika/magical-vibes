package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoutingTrekTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers any number of basic land cards and no other cards")
    void offersBasicLandsOnly() {
        Card plains = new Plains();
        Card forest = new Forest();
        setLibrary(List.of(new GrizzlyBears(), plains, new DiabolicTutor(), forest));

        cast();

        PendingInteraction.SearchLibraryToTopChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.pool()).containsExactlyInAnyOrder(plains, forest);
        assertThat(choice.pool()).allMatch(card -> card.hasType(CardType.LAND)
                && card.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Choosing multiple basic lands puts them on top in the chosen order")
    void choosingMultipleBasicLandsPutsThemOnTop() {
        Card plains = new Plains();
        Card forest = new Forest();
        setLibrary(List.of(plains, new GrizzlyBears(), forest));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(plains.getId(), forest.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(forest);
        assertThat(gd.playerDecks.get(player1.getId()).get(1)).isSameAs(plains);
    }

    @Test
    @DisplayName("Choosing no basic lands leaves the library intact")
    void choosingNoBasicLandsLeavesLibraryIntact() {
        Card forest = new Forest();
        Card nonland = new GrizzlyBears();
        setLibrary(List.of(nonland, forest));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonland, forest);
    }

    @Test
    @DisplayName("No basic lands in the library does not prompt")
    void noBasicLandsDoesNotPrompt() {
        setLibrary(List.of(new GrizzlyBears()));

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class)).isNull();
    }

    private void cast() {
        harness.setHand(player1, List.of(new ScoutingTrek()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(List<Card> cards) {
        GameData gameData = harness.getGameData();
        gameData.playerDecks.get(player1.getId()).clear();
        gameData.playerDecks.get(player1.getId()).addAll(cards);
    }
}
