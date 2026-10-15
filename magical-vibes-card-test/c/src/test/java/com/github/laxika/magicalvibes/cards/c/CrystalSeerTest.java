package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CrystalSeer.class)
class CrystalSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Crystal Seer triggers a top-four library reorder")
    void resolvingTriggersTopFourReorder() {
        castCrystalSeer();

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .hasSize(4);
    }

    @Test
    @DisplayName("Crystal Seer reorder changes the top four cards of its controller's library")
    void reorderChangesTopCards() {
        Card originalTop0 = new CrystalSeer();
        Card originalTop1 = new CrystalSeer();
        Card originalTop2 = new CrystalSeer();
        Card originalTop3 = new CrystalSeer();
        Card untouched = new CrystalSeer();
        harness.setLibrary(player1, List.of(originalTop0, originalTop1, originalTop2, originalTop3, untouched));
        castCrystalSeer();

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());

        resolveAllTriggers();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(deck).containsExactly(originalTop3, originalTop2, originalTop1, originalTop0, untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Crystal Seer reorders all available cards when its library has fewer than four")
    void reordersAllAvailableCardsInShortLibrary() {
        Card first = new CrystalSeer();
        Card second = new CrystalSeer();
        harness.setLibrary(player1, List.of(first, second));

        castCrystalSeer();
        resolveAllTriggers();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("Crystal Seer returns itself to its owner's hand when its ability resolves")
    void returnsItselfToHandWhenAbilityResolves() {
        harness.addToBattlefieldAndReturn(player1, new CrystalSeer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        harness.assertOnBattlefield(player1, "Crystal Seer");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crystal Seer");
        harness.assertInHand(player1, "Crystal Seer");
    }

    @Test
    @DisplayName("Crystal Seer returns to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        CrystalSeer seerCard = new CrystalSeer();
        seerCard.setOwnerId(player1.getId());
        harness.addToBattlefieldAndReturn(player2, seerCard);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, null);

        harness.assertOnBattlefield(player2, "Crystal Seer");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Crystal Seer");
        harness.assertInHand(player1, "Crystal Seer");
        harness.assertNotInHand(player2, "Crystal Seer");
    }

    private void castCrystalSeer() {
        harness.castFromHand(player1, new CrystalSeer(), "{4}{U}");
    }

    @Test
    @DisplayName("Crystal Seer lets only its controller see the sole card in their library")
    void looksAtSingleAvailableCardPrivately() {
        Card onlyCard = new CrystalSeer();
        harness.setLibrary(player1, List.of(onlyCard));
        castCrystalSeer();
        harness.clearMessages();

        resolveAllTriggers();

        String cardId = onlyCard.getId().toString();
        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains(cardId));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains(cardId));
    }

    @Test
    @DisplayName("Crystal Seer's enter trigger resolves normally with an empty library")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castCrystalSeer();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Crystal Seer");
    }

    @Test
    @DisplayName("Crystal Seer's enter trigger still reorders its controller's library after it returns to hand")
    void enterTriggerResolvesAfterSourceReturnsToHand() {
        Card first = new CrystalSeer();
        Card second = new CrystalSeer();
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new CrystalSeer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crystal Seer");
        harness.assertInHand(player1, "Crystal Seer");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
