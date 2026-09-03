package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MangarasTome.class, Disenchant.class, ObNixilisUnshackled.class})
class MangarasTomeTest extends BaseCardTest {

    private List<Card> deck() {
        return gd.playerDecks.get(player1.getId());
    }

    private List<String> handNames() {
        return gd.playerHands.get(player1.getId()).stream().map(Card::getName).toList();
    }

    private void castTome(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new MangarasTome()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve the artifact → ETB trigger on stack
        harness.passBothPriorities(); // resolve the ETB trigger → library search
    }

    /** Puts the Tome onto the battlefield with a ready-made pile, bypassing the ETB search. */
    private UUID setupWithPile(List<Card> pile) {
        harness.addToBattlefield(player1, new MangarasTome());
        UUID permId = harness.getPermanentId(player1, "Mangara's Tome");
        for (Card card : pile) {
            gd.addToExile(player1.getId(), card, permId, true);
        }
        return permId;
    }

    private void activateTome() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities(); // resolve the ability — registers the delayed next-draw replacement
    }

    @Test
    @DisplayName("ETB search offers the whole library and exiles five cards face down with the Tome")
    void etbExilesFiveCardsIntoPile() {
        castTome(List.of(new Disenchant(), new Disenchant(), new Disenchant(), new Disenchant(), new Disenchant(),
                new Disenchant(), new Disenchant()));

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(7);

        for (int i = 0; i < 5; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        UUID permId = harness.getPermanentId(player1, "Mangara's Tome");
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(5);
        assertThat(gd.exiledCards).filteredOn(e -> permId.equals(e.sourcePermanentId())).allMatch(e -> e.faceDown());
        assertThat(deck()).hasSize(2);
    }

    @Test
    @DisplayName("A search with enough cards cannot stop before finding five cards")
    void etbMustFindRequestedCardsWhenLibraryHasEnough() {
        castTome(List.of(new Disenchant(), new Disenchant(), new Disenchant(), new Disenchant(),
                new Disenchant(), new Disenchant()));

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot fail to find with an unrestricted search");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        UUID permId = harness.getPermanentId(player1, "Mangara's Tome");
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
        assertThat(deck()).hasSize(6);
    }

    @Test
    @DisplayName("A library smaller than five is exhausted without an endless prompt")
    void etbWithSmallLibraryExilesWholeLibrary() {
        castTome(List.of(new Disenchant(), new Disenchant()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getCardsExiledByPermanent(harness.getPermanentId(player1, "Mangara's Tome"))).hasSize(2);
        assertThat(deck()).isEmpty();
    }

    @Test
    @DisplayName("An empty library is searched without creating a choice")
    void etbWithEmptyLibrary() {
        castTome(List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getCardsExiledByPermanent(harness.getPermanentId(player1, "Mangara's Tome"))).isEmpty();
        assertThat(deck()).isEmpty();
    }

    @Test
    @DisplayName("Searching an empty library still triggers an opponent's search ability")
    void emptyLibrarySearchTriggersOpponentSearchAbility() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLife(player1, 20);

        castTome(List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("The ETB search still resolves after the Tome leaves the battlefield")
    void etbResolvesAfterTomeLeavesBattlefield() {
        harness.setLibrary(player1, List.of(new Disenchant(), new Disenchant(), new Disenchant(),
                new Disenchant(), new Disenchant(), new Disenchant()));
        harness.setHand(player1, List.of(new MangarasTome()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        var tome = findPermanent(player1, "Mangara's Tome");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, tome));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("The activated ability replaces the next draw with the top card of the pile")
    void activationReplacesNextDraw() {
        setupWithPile(List.of(new Disenchant()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MangarasTome(), new Disenchant()));

        activateTome();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(handNames()).containsExactly("Disenchant");
        // No card was drawn — the library is untouched.
        assertThat(deck()).hasSize(2);
        assertThat(deck().getFirst().getName()).isEqualTo("Mangara's Tome");
    }

    @Test
    @DisplayName("Only the next draw is replaced — a later draw is an ordinary draw")
    void replacementIsOneShot() {
        setupWithPile(List.of(new Disenchant(), new MangarasTome()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MangarasTome(), new Disenchant()));

        activateTome();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(handNames()).containsExactly("Disenchant", "Mangara's Tome");
        assertThat(deck()).hasSize(1);
    }

    @Test
    @DisplayName("Two activations replace the next two draws, taking the pile from the top down")
    void twoActivationsReplaceTwoDraws() {
        setupWithPile(List.of(new Disenchant(), new MangarasTome()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MangarasTome(), new Disenchant()));

        activateTome();
        activateTome();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(handNames()).containsExactly("Disenchant", "Mangara's Tome");
        assertThat(deck()).hasSize(2);
    }

    @Test
    @DisplayName("With an empty pile the draw is still replaced — no card is drawn")
    void emptyPileReplacesDrawWithNothing() {
        setupWithPile(List.of());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MangarasTome(), new Disenchant()));

        activateTome();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(handNames()).isEmpty();
        assertThat(deck()).hasSize(2);
    }

    @Test
    @DisplayName("The delayed replacement expires at end of turn if the player never draws")
    void replacementExpiresAtCleanup() {
        setupWithPile(List.of(new Disenchant()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MangarasTome(), new Disenchant()));

        activateTome();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(handNames()).containsExactly("Mangara's Tome");
    }
}
