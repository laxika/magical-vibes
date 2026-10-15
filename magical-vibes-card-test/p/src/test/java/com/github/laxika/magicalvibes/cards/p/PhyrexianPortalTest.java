package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.ShieldSphere;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianPortal.class, ShieldSphere.class})
class PhyrexianPortalTest extends BaseCardTest {

    /** Ten distinct cards so every pile member is identifiable by instance. */
    private List<Card> tenCardLibrary() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            library.add(new ShieldSphere());
        }
        return library;
    }

    private List<Card> activateAndReachSeparation(List<Card> library) {
        harness.addToBattlefield(player1, new PhyrexianPortal());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        return library;
    }

    @Test
    @DisplayName("Activating takes the top ten cards and asks the opponent to split them")
    void opponentIsPromptedToSeparateTopTen() {
        activateAndReachSeparation(tenCardLibrary());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).hasSize(10);
    }

    @Test
    @DisplayName("The chosen pile is searched for a card and the unchosen pile is exiled")
    void chosenPileIsSearchedAndOtherPileExiled() {
        List<Card> library = activateAndReachSeparation(tenCardLibrary());
        List<Card> pile1 = library.subList(0, 4);
        List<Card> pile2 = library.subList(4, 10);

        harness.handleMultipleCardsChosen(player2, pile1.stream().map(Card::getId).toList());

        // Controller picks Pile 1 to search; Pile 2 is exiled.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibraryRevealChoice search =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.playerId()).isEqualTo(player1.getId());
        assertThat(search.validCardIds()).hasSize(4);

        Card wanted = pile1.getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(wanted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wanted);
        // The rest of the searched pile went back into the library; Pile 2 never returns.
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(pile1.subList(1, 4));
        List<UUID> exiledIds = pile2.stream().map(Card::getId).toList();
        assertThat(exiledIds).allSatisfy(id -> assertThat(gd.findExiledCard(id)).isNotNull());
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Declining searches the other pile instead")
    void decliningSearchesPileTwo() {
        List<Card> library = activateAndReachSeparation(tenCardLibrary());
        List<Card> pile1 = library.subList(0, 4);
        List<Card> pile2 = library.subList(4, 10);

        harness.handleMultipleCardsChosen(player2, pile1.stream().map(Card::getId).toList());
        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.LibraryRevealChoice search =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.validCardIds()).hasSize(6);

        Card wanted = pile2.getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(wanted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wanted);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(pile2.subList(1, 6));
        assertThat(pile1).allSatisfy(card -> assertThat(gd.findExiledCard(card.getId())).isNotNull());
    }

    @Test
    @DisplayName("Searching a nonempty pile requires finding one card")
    void searchMustFindOneCard() {
        List<Card> library = activateAndReachSeparation(tenCardLibrary());
        List<Card> pile1 = library.subList(0, 4);

        harness.handleMultipleCardsChosen(player2, pile1.stream().map(Card::getId).toList());
        harness.handleMayAbilityChosen(player1, true);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        Card wanted = pile1.getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(wanted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wanted);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(pile1.subList(1, 4));
    }

    @Test
    @CardUsed({PhyrexianPortal.class, ShieldSphere.class, PsychogenicProbe.class})
    @DisplayName("Searching an empty pile still shuffles the library")
    void emptyPileStillCausesShuffle() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        activateAndReachSeparation(tenCardLibrary());

        harness.handleMultipleCardsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Only the top ten cards are divided and the remaining library is preserved")
    void deeperLibraryCardsAreNotIncludedInPiles() {
        List<Card> library = tenCardLibrary();
        Card deeperCard = new ShieldSphere();
        library.add(deeperCard);
        activateAndReachSeparation(library);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(
                library.subList(0, 10).stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(deeperCard);

        Card wanted = library.getFirst();
        harness.handleMultipleCardsChosen(player2, List.of(wanted.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(wanted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wanted);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(deeperCard);
        assertThat(library.subList(1, 10))
                .allSatisfy(card -> assertThat(gd.findExiledCard(card.getId())).isNotNull());
    }

    @Test
    @DisplayName("The searched card is not revealed in the public log")
    void searchedCardRemainsPrivate() {
        List<Card> library = activateAndReachSeparation(tenCardLibrary());
        Card wanted = library.getFirst();
        harness.handleMultipleCardsChosen(player2, List.of(wanted.getId()));
        harness.handleMayAbilityChosen(player1, true);
        int previousLogSize = gd.gameLog.size();

        harness.handleMultipleCardsChosen(player1, List.of(wanted.getId()));

        assertThat(gd.gameLog.subList(previousLogSize, gd.gameLog.size()))
                .allSatisfy(entry -> assertThat(entry.plainText()).doesNotContain(wanted.getName()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wanted);
    }

    @Test
    @DisplayName("An empty chosen pile ends the ability with nothing searched")
    void emptyChosenPileEndsTheAbility() {
        List<Card> library = activateAndReachSeparation(tenCardLibrary());

        // Opponent puts every card into Pile 2, leaving Pile 1 empty.
        harness.handleMultipleCardsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(library).allSatisfy(card -> assertThat(gd.findExiledCard(card.getId())).isNotNull());
    }

    @Test
    @DisplayName("Nothing happens when the library has fewer than ten cards")
    void fewerThanTenCardsDoesNothing() {
        List<Card> library = List.of(new ShieldSphere(), new ShieldSphere(), new ShieldSphere());
        activateAndReachSeparation(library);

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ten-card condition is checked when the ability resolves")
    void librarySizeIsCheckedAtResolution() {
        harness.addToBattlefield(player1, new PhyrexianPortal());
        harness.setLibrary(player1, tenCardLibrary());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, player2.getId());

        List<Card> reducedLibrary = tenCardLibrary().subList(0, 9);
        harness.setLibrary(player1, reducedLibrary);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(reducedLibrary);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new PhyrexianPortal());
        harness.setLibrary(player1, tenCardLibrary());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new PhyrexianPortal());
        harness.addToBattlefield(player2, new ShieldSphere());
        harness.setLibrary(player1, tenCardLibrary());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID shieldSphere = harness.getPermanentId(player2, "Shield Sphere");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shieldSphere))
                .isInstanceOf(IllegalStateException.class);
    }
}
