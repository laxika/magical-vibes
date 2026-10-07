package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrostRaptor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurgingDementia.class, FrostRaptor.class})
class SurgingDementiaTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards a card")
    void targetPlayerDiscards() {
        harness.setHand(player2, List.of(new FrostRaptor()));
        castSurgingDementia(player2.getId());

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Frost Raptor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ripple can free-cast a revealed Surging Dementia")
    void rippleFreeCastsMatchingSpell() {
        harness.setLibrary(player1, List.of(new SurgingDementia(), new FrostRaptor()));
        harness.setHand(player2, List.of(new FrostRaptor(), new FrostRaptor()));

        castSurgingDementia(player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Frost Raptor");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Surging Dementia", "Surging Dementia");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ripple can be declined without revealing the library")
    void rippleCanBeDeclined() {
        List<Card> libraryTop = List.of(new FrostRaptor());
        harness.setLibrary(player1, libraryTop);
        harness.setHand(player2, List.of(new FrostRaptor()));

        castSurgingDementia(player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(libraryTop);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new FrostRaptor());
        harness.setHand(player1, List.of(new SurgingDementia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Frost Raptor")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ripple goes on the stack before the controller decides whether to reveal")
    void rippleRevealChoiceWaitsForResolution() {
        harness.setLibrary(player1, List.of(new FrostRaptor()));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new SurgingDementia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Surging Dementia");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The targeted player chooses which card to discard")
    void targetChoosesDiscard() {
        FrostRaptor retained = new FrostRaptor();
        SurgingDementia discarded = new SurgingDementia();
        harness.setHand(player2, List.of(retained, discarded));

        castSurgingDementia(player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surging Dementia can target its caster")
    void canTargetCaster() {
        castSurgingDementia(player1.getId());
        FrostRaptor discarded = new FrostRaptor();
        harness.setHand(player1, List.of(discarded));
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library and an empty target hand do not prevent resolution")
    void emptyLibraryAndHand() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of());

        castSurgingDementia(player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Surging Dementia");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Ripple reveals only four cards and bottoms declined spells in the chosen order")
    void rippleDeclinedSpellGoesToBottom() {
        SurgingDementia revealedSpell = new SurgingDementia();
        FrostRaptor first = new FrostRaptor();
        FrostRaptor second = new FrostRaptor();
        FrostRaptor third = new FrostRaptor();
        SurgingDementia unrevealedSpell = new SurgingDementia();
        harness.setLibrary(player1, List.of(revealedSpell, first, second, third, unrevealedSpell));
        harness.setHand(player2, List.of());

        castSurgingDementia(player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(revealedSpell, first, second, third);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(unrevealedSpell, third, second, first, revealedSpell);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castSurgingDementia(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new SurgingDementia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
