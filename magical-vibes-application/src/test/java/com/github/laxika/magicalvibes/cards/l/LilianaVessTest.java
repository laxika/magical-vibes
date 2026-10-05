package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({LilianaVess.class, GrizzlyBears.class, Plains.class, Swamp.class, SoulWarden.class, GrafdiggersCage.class})
class LilianaVessTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new LilianaVess(), "{3}{B}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Liliana Vess");
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 5")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.castFromHand(player1, new LilianaVess(), "{3}{B}{B}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Liliana Vess"));
        Permanent liliana = bf.stream().filter(p -> p.getCard().getName().equals("Liliana Vess")).findFirst().orElseThrow();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(liliana.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("+1 ability makes target player discard a card and increases loyalty")
    void plusOneMakesTargetPlayerDiscard() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 5 + 1
        // Player is prompted to choose a card to discard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("+1 ability can target self")
    void plusOneCanTargetSelf() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 5 + 1
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("-2 ability triggers library search and decreases loyalty")
    void minusTwoTriggersLibrarySearch() {
        Permanent liliana = addReadyLiliana(player1);
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 5 - 2
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // All cards from library should be offered (unrestricted search)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(4);
    }

    @Test
    @DisplayName("-2 ability puts chosen card on top of library")
    void minusTwoPutsCardOnTop() {
        addReadyLiliana(player1);
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        // Find Grizzly Bears in the offered cards
        int bearsIndex = -1;
        for (int i = 0; i < offered.size(); i++) {
            if (offered.get(i).getName().equals("Grizzly Bears")) {
                bearsIndex = i;
                break;
            }
        }
        assertThat(bearsIndex).isGreaterThanOrEqualTo(0);

        harness.handleCardChosen(player1, bearsIndex);

        // The chosen card should be on top of the library
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).isNotEmpty();
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("-2 ability is unrestricted search (cannot fail to find)")
    void minusTwoCannotFailToFind() {
        addReadyLiliana(player1);
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("-8 ability puts all creature cards from all graveyards onto battlefield under controller's control")
    void minusEightPutsAllCreaturesFromAllGraveyards() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 8);

        // Put creature cards into both graveyards
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears1));
        harness.setGraveyard(player2, List.of(bears2, bears3));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // Liliana should have 0 loyalty (8 - 8) and be in graveyard
        harness.assertNotOnBattlefield(player1, "Liliana Vess");

        // All three creatures should be on player1's battlefield
        long bearsOnBf = countPermanents(player1, "Grizzly Bears");
        assertThat(bearsOnBf).isEqualTo(3);

        // Both graveyards should have no creature cards remaining
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-8 ability does not put non-creature cards onto battlefield")
    void minusEightDoesNotPutNonCreatures() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 8);

        // Put a non-creature card into graveyard
        harness.setGraveyard(player2, List.of(new Plains(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // Only the creature should be on battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Plains");

        // Plains should still be in opponent's graveyard
        harness.assertInGraveyard(player2, "Plains");
    }

    @Test
    @DisplayName("Cannot use -8 when loyalty is only 5")
    void cannotActivateMinusEightWithInsufficientLoyalty() {
        Permanent liliana = addReadyLiliana(player1);
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyLiliana(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyLiliana(player1);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        // Finish the +1 (the discard it prompts for) before trying again: activating through the
        // harness clears any open prompt, which would strand the ability mid-resolution.
        harness.handleCardChosen(player2, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    void plusOneResolvesAgainstEmptyHand() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetPlayerChoosesExactlyOneCardToDiscard() {
        addReadyLiliana(player1);
        GrizzlyBears bears = new GrizzlyBears();
        Swamp swamp = new Swamp();
        harness.setHand(player2, List.of(bears, swamp));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(swamp);
    }

    @Test
    void minusTwoResolvesWithEmptyLibrary() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void minusEightReturningCreaturesSeeEachOtherEnter() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 9);
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new SoulWarden(), new SoulWarden()));
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    void minusEightBlockedCreaturesStayInTheirOriginalGraveyards() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 9);
        harness.addToBattlefield(player2, new GrafdiggersCage());
        GrizzlyBears ownBears = new GrizzlyBears();
        GrizzlyBears opposingBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownBears));
        harness.setGraveyard(player2, List.of(opposingBears));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownBears);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingBears);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addReadyLiliana(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LilianaVess());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
