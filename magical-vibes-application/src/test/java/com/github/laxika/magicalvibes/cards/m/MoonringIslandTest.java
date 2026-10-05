package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cursecatcher;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonringIsland.class, Cursecatcher.class, Island.class, ThoughtReflection.class})
class MoonringIslandTest extends BaseCardTest {

    @Test
    @DisplayName("Look ability begins a private look at the top card when controlling two or more blue permanents")
    void looksWithTwoBluePermanents() {
        Card topCard = setTopCard(player2.getId(), new Island());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        Permanent island = addMoonring(player1);
        addBluePermanent(player1);
        addBluePermanent(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(island);
        harness.activateAbility(player1, idx, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameLogContains("looks at the top card")).isTrue();

        // Closing the look leaves the library untouched, in order.
        harness.handleCardChosen(player1, -1);
        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter).hasSize(deckSizeBefore);
        assertThat(deckAfter.getFirst().getId()).isEqualTo(topCard.getId());
    }

    @Test
    @DisplayName("Look ability cannot be activated with fewer than two blue permanents")
    void rejectedWithTooFewBluePermanents() {
        Permanent island = addMoonring(player1);
        // One blue permanent — the colorless Moonring Island does not make up the second.
        addBluePermanent(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(island);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability adds blue mana")
    void manaAbilityAddsBlue() {
        Permanent island = addMoonring(player1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(island);
        harness.activateAbility(player1, idx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new MoonringIsland()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Moonring Island").isTapped()).isTrue();
    }

    @Test
    void lookPaysBlueManaAndTapsLand() {
        Permanent island = addMoonring(player1);
        addBluePermanent(player1);
        addBluePermanent(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(island.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsBluePermanentsAndColorlessIslandsDoNotSatisfyRestriction() {
        Permanent island = addMoonring(player1);
        addBluePermanent(player1);
        addBluePermanent(player2);
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(island.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void canLookAtOwnLibraryWithoutChangingItsOrder() {
        Card topCard = new Island();
        Card secondCard = new Cursecatcher();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        addMoonring(player1);
        addBluePermanent(player1);
        addBluePermanent(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch look =
                (PendingInteraction.LibrarySearch) gd.interaction.activeInteraction();
        assertThat(look.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(look.params().cards()).containsExactly(topCard);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
    }

    @Test
    void emptyLibraryDoesNotRequestInput() {
        harness.setLibrary(player2, List.of());
        addMoonring(player1);
        addBluePermanent(player1);
        addBluePermanent(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingBluePermanentsAfterActivationDoesNotPreventLooking() {
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        addMoonring(player1);
        Permanent bluePermanent = harness.addToBattlefieldAndReturn(player1, new Cursecatcher());
        addBluePermanent(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bluePermanent);
        gd.playerGraveyards.get(player1.getId()).add(bluePermanent.getCard());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch look =
                (PendingInteraction.LibrarySearch) gd.interaction.activeInteraction();
        assertThat(look.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(look.params().cards()).containsExactly(topCard);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    void blueNoncreaturePermanentsSatisfyRestriction() {
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        addMoonring(player1);
        addBluePermanent(player1);
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch look =
                (PendingInteraction.LibrarySearch) gd.interaction.activeInteraction();
        assertThat(look.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(look.params().cards()).containsExactly(topCard);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    void lookCannotBeActivatedWithoutBlueMana() {
        Permanent island = addMoonring(player1);
        addBluePermanent(player1);
        addBluePermanent(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(island.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addMoonring(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MoonringIsland());
    }

    private void addBluePermanent(Player player) {
        harness.addToBattlefield(player, new Cursecatcher());
    }

    private Card setTopCard(UUID playerId, Card card) {
        List<Card> deck = gd.playerDecks.get(playerId);
        deck.addFirst(card);
        return card;
    }
}
