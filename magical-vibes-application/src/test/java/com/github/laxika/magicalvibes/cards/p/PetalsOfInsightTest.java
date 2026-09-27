package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PetalsOfInsight.class, HumbleBudoka.class, LanternKami.class, Mountain.class,
        SakuraTribeElder.class})
class PetalsOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers the choice between bottoming the three cards and drawing them")
    void resolvingOffersChoice() {
        castPetals();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining draws three cards and Petals of Insight goes to the graveyard")
    void decliningDrawsThreeCards() {
        castPetals();

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Sakura-Tribe Elder", "Lantern Kami", "Humble Budoka");
        harness.assertInGraveyard(player1, "Petals of Insight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting bottoms the three cards in a chosen order and returns Petals of Insight to hand")
    void acceptingBottomsCardsAndReturnsSpell() {
        castPetals();

        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck).extracting(Card::getName)
                .containsExactly("Mountain", "Mountain", "Humble Budoka", "Sakura-Tribe Elder", "Lantern Kami");

        harness.assertInHand(player1, "Petals of Insight");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting with a short library bottoms all available cards and returns Petals of Insight to hand")
    void acceptingWithShortLibrary() {
        castPetals(List.of(new SakuraTribeElder(), new LanternKami()));

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibraryReorder reorder =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).extracting(Card::getName)
                .containsExactly("Sakura-Tribe Elder", "Lantern Kami");

        harness.getGameService().handleInteractionAnswer(harness.getGameData(), player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(harness.getGameData().playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Lantern Kami", "Sakura-Tribe Elder");
        harness.assertInHand(player1, "Petals of Insight");
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    /** Sets a known five-card library and resolves Petals of Insight up to its may-choice. */
    private void castPetals() {
        castPetals(List.of(new SakuraTribeElder(), new LanternKami(), new HumbleBudoka(),
                new Mountain(), new Mountain()));
    }

    private void castPetals(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new PetalsOfInsight(), "{4}{U}");
        harness.passBothPriorities();
    }
}
