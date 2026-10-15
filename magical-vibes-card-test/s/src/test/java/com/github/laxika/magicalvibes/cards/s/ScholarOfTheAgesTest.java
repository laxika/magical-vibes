package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.m.MoatPiranhas;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScholarOfTheAges.class, Negate.class, Disentomb.class, MoatPiranhas.class})
class ScholarOfTheAgesTest extends BaseCardTest {

    private void castScholar() {
        harness.castFromHand(player1, new ScholarOfTheAges(), "{5}{U}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB prompts for up to two instant or sorcery cards")
    void etbPromptsForInstantOrSorceryCards() {
        harness.setGraveyard(player1, List.of(new Negate(), new Disentomb(), new MoatPiranhas()));

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

        castScholar();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Negate");
        harness.assertInGraveyard(player1, "Disentomb");
    }

    @Test
    void mayChooseZeroWithLegalTargets() {
        harness.setGraveyard(player1, List.of(new Negate(), new Disentomb()));

        castScholar();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Negate");
        harness.assertInGraveyard(player1, "Disentomb");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayReturnTwoInstantsOrTwoSorceries() {
        for (boolean instants : List.of(true, false)) {
            Card first = instants ? new Negate() : new Disentomb();
            Card second = instants ? new Negate() : new Disentomb();
            harness.setGraveyard(player1, List.of(first, second));

            castScholar();
            harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        }
    }

    @Test
    void returnsRemainingLegalTargetWithoutChoosingReplacement() {
        Card instant = new Negate();
        Card sorcery = new Disentomb();
        Card unchosen = new Negate();
        harness.setGraveyard(player1, List.of(instant, sorcery, unchosen));

        castScholar();
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId()));
        harness.setGraveyard(player1, List.of(sorcery, unchosen));
        harness.setExile(player1, List.of(instant));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unchosen);
    }

    @Test
    void opponentGraveyardIsNotEligible() {
        harness.setGraveyard(player1, List.of(new MoatPiranhas()));
        harness.setGraveyard(player2, List.of(new Negate(), new Disentomb()));

        castScholar();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Negate");
        harness.assertInGraveyard(player2, "Disentomb");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No instant or sorcery cards skips the graveyard prompt")
    void noInstantOrSorceryCardsSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new MoatPiranhas()));

        castScholar();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Scholar of the Ages");
        harness.assertInGraveyard(player1, "Moat Piranhas");
    }
}
