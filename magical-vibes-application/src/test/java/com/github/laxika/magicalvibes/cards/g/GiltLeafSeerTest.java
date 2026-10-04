package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiltLeafSeer.class, LeafGilder.class})
class GiltLeafSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating puts the ability on the stack")
    void activationStacksAbility() {
        Permanent seer = addCreatureReady(player1, new GiltLeafSeer());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(seer.getCard());
    }

    @Test
    @DisplayName("Resolving enters library reorder state for top two cards")
    void resolvingEntersReorderForTopTwo() {
        addCreatureReady(player1, new GiltLeafSeer());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Reorder swaps the top two cards of the library")
    void reorderSwapsTopTwo() {
        addCreatureReady(player1, new GiltLeafSeer());
        harness.addMana(player1, ManaColor.GREEN, 1);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(deck.get(0)).isSameAs(originalTop1);
        assertThat(deck.get(1)).isSameAs(originalTop0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Library with a single card just looks, no reorder needed")
    void libraryWithOneCard() {
        addCreatureReady(player1, new GiltLeafSeer());
        harness.addMana(player1, ManaColor.GREEN, 1);

        GameData gd = harness.getGameData();
        Card only = new LeafGilder();
        harness.setLibrary(player1, List.of(only));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(deck).containsExactly(only);
    }

    @Test
    @DisplayName("Cannot activate when summoning sick")
    void cannotActivateWhenSummoningSick() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new GiltLeafSeer());
        perm.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller sees the sole library card without revealing it to the opponent")
    void privatelyShowsSoleLibraryCard() {
        addCreatureReady(player1, new GiltLeafSeer());
        LeafGilder only = new LeafGilder();
        harness.setLibrary(player1, List.of(only));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.clearMessages();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("Leaf Gilder"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Leaf Gilder"));
    }

    @Test
    @DisplayName("Activation pays one green mana and taps the Seer")
    void paysManaAndTapCosts() {
        Permanent seer = addCreatureReady(player1, new GiltLeafSeer());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutGreenMana() {
        Permanent seer = addCreatureReady(player1, new GiltLeafSeer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(seer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate an already tapped Seer")
    void cannotActivateWhenTapped() {
        Permanent seer = addCreatureReady(player1, new GiltLeafSeer());
        seer.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library resolves without drawing or asking for an order")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new GiltLeafSeer());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(originalHand);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can keep the original order without disturbing other cards or the opponent's library")
    void canKeepOriginalOrder() {
        addCreatureReady(player1, new GiltLeafSeer());
        Card first = new LeafGilder();
        Card second = new GiltLeafSeer();
        Card third = new LeafGilder();
        Card opponentTop = new LeafGilder();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
