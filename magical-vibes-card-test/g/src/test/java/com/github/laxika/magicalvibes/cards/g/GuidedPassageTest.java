package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
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

    @Test
    @DisplayName("A library containing only creatures supplies exactly one creature")
    void choosesFromCreatureOnlyLibrary() {
        Card chosen = new GaeasSkyfolk();
        Card remaining = new GaeasSkyfolk();
        castGuidedPassage(List.of(chosen, remaining));

        harness.handleMultipleCardsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library containing only lands supplies exactly one land")
    void choosesFromLandOnlyLibrary() {
        Card chosen = new YavimayaCoast();
        Card remaining = new YavimayaCoast();
        castGuidedPassage(List.of(chosen, remaining));

        harness.handleMultipleCardsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library containing only noncreature nonland cards supplies exactly one card")
    void choosesFromSpellOnlyLibrary() {
        Card chosen = new JadedResponse();
        Card remaining = new JadedResponse();
        castGuidedPassage(List.of(chosen, remaining));

        harness.handleMultipleCardsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent cannot decline to choose an available card")
    void cannotFailToChooseAvailableCard() {
        Card creature = new GaeasSkyfolk();
        castGuidedPassage(List.of(creature));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of()))
                .hasMessageContaining("exactly one creature");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleMultipleCardsChosen(player2, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing two creatures is rejected without moving any cards")
    void rejectsMultipleCardsOfSameCategory() {
        Card creature = new GaeasSkyfolk();
        Card otherCreature = new GaeasSkyfolk();
        Card land = new YavimayaCoast();
        castGuidedPassage(List.of(creature, otherCreature, land));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2,
                List.of(creature.getId(), otherCreature.getId(), land.getId())))
                .hasMessageContaining("exactly one creature");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, otherCreature, land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleMultipleCardsChosen(player2, List.of(otherCreature.getId(), land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(otherCreature, land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(DryadArbor.class)
    @DisplayName("A land creature can be the creature choice alongside a separate land")
    void landCreatureCanBeChosenAsCreature() {
        Card creature = new DryadArbor();
        Card land = new YavimayaCoast();
        Card spell = new JadedResponse();
        castGuidedPassage(List.of(creature, land, spell));

        harness.handleMultipleCardsChosen(player2,
                List.of(creature.getId(), land.getId(), spell.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land, spell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Guided Passage");
    }

    @Test
    @CardUsed(DryadArbor.class)
    @DisplayName("A land creature can be the land choice alongside a separate creature")
    void landCreatureCanBeChosenAsLand() {
        Card creature = new GaeasSkyfolk();
        Card land = new DryadArbor();
        Card spell = new JadedResponse();
        castGuidedPassage(List.of(creature, land, spell));

        harness.handleMultipleCardsChosen(player2,
                List.of(creature.getId(), land.getId(), spell.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(creature, land, spell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Guided Passage");
    }

    private void castGuidedPassage(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new GuidedPassage(), "{G}{U}{R}");
        harness.passBothPriorities();
    }
}
