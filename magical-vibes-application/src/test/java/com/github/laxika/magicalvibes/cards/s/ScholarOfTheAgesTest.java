package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ScholarOfTheAgesTest extends BaseCardTest {

    private void castScholar() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB prompts for up to two instant or sorcery cards")
    void etbPromptsForInstantOrSorceryCards() {
        harness.setGraveyard(player1, List.of(new Negate(), new Disentomb(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ScholarOfTheAges()));

        castScholar();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                gd.playerGraveyards.get(player1.getId()).get(0).getId(),
                gd.playerGraveyards.get(player1.getId()).get(1).getId());
    }

    @Test
    @DisplayName("Returns a chosen instant and sorcery card to hand")
    void returnsChosenInstantAndSorceryCards() {
        Card instant = new Negate();
        Card sorcery = new Disentomb();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(new ScholarOfTheAges()));

        castScholar();

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scholar of the Ages");
        harness.assertInHand(player1, "Negate");
        harness.assertInHand(player1, "Disentomb");
        harness.assertNotInGraveyard(player1, "Negate");
        harness.assertNotInGraveyard(player1, "Disentomb");
    }

    @Test
    @DisplayName("Choosing one card returns only that card")
    void choosingOneCardReturnsOnlyThatCard() {
        Card instant = new Negate();
        Card sorcery = new Disentomb();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(new ScholarOfTheAges()));

        castScholar();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Negate");
        harness.assertInGraveyard(player1, "Disentomb");
    }

    @Test
    @DisplayName("No instant or sorcery cards skips the graveyard prompt")
    void noInstantOrSorceryCardsSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ScholarOfTheAges()));

        castScholar();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Scholar of the Ages");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
