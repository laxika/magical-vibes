package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JadedResponse;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuidedPassage.class, GaeasSkyfolk.class, YavimayaCoast.class, JadedResponse.class})
class GuidedPassageTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses a creature, land, and noncreature nonland card for the hand")
    void opponentChoosesOneCardOfEachCategory() {
        Card creature = new GaeasSkyfolk();
        Card land = new YavimayaCoast();
        Card spell = new JadedResponse();
        Card untouched = new JadedResponse();
        castGuidedPassage(List.of(creature, land, spell, untouched));

        PendingInteraction.GuidedPassageChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GuidedPassageChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), land.getId(), spell.getId(),
                untouched.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(creature.getId(), land.getId(), spell.getId())))
                .hasMessageContaining("Not your turn");

        harness.handleMultipleCardsChosen(player2,
                List.of(creature.getId(), land.getId(), spell.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature, land, spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Guided Passage");
    }

    @Test
    @DisplayName("An available category must be represented exactly once")
    void rejectsMissingCategory() {
        Card creature = new GaeasSkyfolk();
        Card land = new YavimayaCoast();
        Card spell = new JadedResponse();
        castGuidedPassage(List.of(creature, land, spell));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2,
                List.of(creature.getId(), land.getId())))
                .hasMessageContaining("noncreature, nonland");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GuidedPassageChoice.class);
    }

    @Test
    @DisplayName("Categories without matching cards are ignored")
    void ignoresMissingCategory() {
        Card creature = new GaeasSkyfolk();
        Card land = new YavimayaCoast();
        castGuidedPassage(List.of(creature, land));

        harness.handleMultipleCardsChosen(player2, List.of(creature.getId(), land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without opening a choice")
    void resolvesEmptyLibrary() {
        castGuidedPassage(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Guided Passage");
    }

    private void castGuidedPassage(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new GuidedPassage(), "{G}{U}{R}");
        harness.passBothPriorities();
    }
}
