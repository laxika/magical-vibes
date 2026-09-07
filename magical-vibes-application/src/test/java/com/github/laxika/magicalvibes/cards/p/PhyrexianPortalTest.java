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
    @DisplayName("Searching may fail to find, sending the whole pile back into the library")
    void searchMayFailToFind() {
        List<Card> library = activateAndReachSeparation(tenCardLibrary());
        List<Card> pile1 = library.subList(0, 4);

        harness.handleMultipleCardsChosen(player2, pile1.stream().map(Card::getId).toList());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(pile1);
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
        harness.addToBattlefield(player1, new PhyrexianPortal());
        List<Card> library = List.of(new ShieldSphere(), new ShieldSphere(), new ShieldSphere());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
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
