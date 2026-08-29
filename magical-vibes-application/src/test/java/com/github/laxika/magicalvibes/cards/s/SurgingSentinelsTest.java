package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SurgingSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("Ripple offers every revealed card with the same name")
    void rippleOffersEverySameNameCard() {
        prepareCaster(List.of(
                new Mountain(),
                new SurgingSentinels(),
                new Forest(),
                new SurgingSentinels()));

        castSentinels();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL
                && entry.getCard().getName().equals("Surging Sentinels"));
    }

    @Test
    @DisplayName("Ripple lets the player decline and order all revealed cards on the bottom")
    void declineOrdersRevealedCardsOnBottom() {
        prepareCaster(List.of(
                new Mountain(),
                new Forest(),
                new Mountain(),
                new Forest()));

        castSentinels();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).hasSize(4);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Mountain", "Forest", "Mountain");
    }

    @Test
    @DisplayName("Ripple can be declined without revealing cards")
    void rippleCanBeDeclined() {
        List<Card> libraryTop = List.of(new Mountain(), new Forest());
        prepareCaster(libraryTop);

        castSentinels();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain", "Forest");
    }

    private void prepareCaster(List<Card> libraryTop) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(libraryTop);
        harness.setHand(player1, List.of(new SurgingSentinels()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castSentinels() {
        harness.castCreature(player1, 0);
    }
}
