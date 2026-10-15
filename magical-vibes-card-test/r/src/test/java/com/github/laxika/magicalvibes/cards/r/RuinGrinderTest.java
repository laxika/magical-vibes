package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NotionThief;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinGrinder.class, GrizzlyBears.class, Mountain.class, Forest.class, NotionThief.class})
class RuinGrinderTest extends BaseCardTest {

    @Test
    @DisplayName("When Ruin Grinder dies, each player may discard their hand and draw seven cards")
    void deathTriggerLetsPlayersDiscardAndDraw() {
        Card player1HandCard = new GrizzlyBears();
        Card player2HandCard = new GrizzlyBears();
        harness.setHand(player1, List.of(player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 7);
        fillLibrary(player2, 7);

        Permanent ruinGrinder = harness.addToBattlefieldAndReturn(player1, new RuinGrinder());
        ruinGrinder.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1HandCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(player2HandCard);
    }

    @Test
    @DisplayName("Mountaincycling discards Ruin Grinder and offers only Mountains")
    void mountaincyclingSearchesForMountain() {
        harness.setHand(player1, List.of(new RuinGrinder()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Mountain(), new Forest(), new Mountain()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ruin Grinder");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof Mountain)
                .hasSize(2);
    }

    @Test
    @DisplayName("Players finish choosing before either hand is discarded or cards are drawn")
    void bothPlayersChooseBeforeDiscarding() {
        Card firstHand = new Mountain();
        Card secondHand = new Forest();
        harness.setHand(player1, List.of(firstHand));
        harness.setHand(player2, List.of(secondHand));
        fillLibrary(player1, 7);
        fillLibrary(player2, 7);

        Permanent grinder = harness.addToBattlefieldAndReturn(player1, new RuinGrinder());
        grinder.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstHand);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(firstHand);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7).doesNotContain(firstHand);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7).doesNotContain(secondHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstHand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondHand);
    }

    @Test
    @DisplayName("A player with an empty hand may still choose to draw seven")
    void emptyHandCanDrawSeven() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        fillLibrary(player1, 7);
        fillLibrary(player2, 7);

        Permanent grinder = harness.addToBattlefieldAndReturn(player1, new RuinGrinder());
        grinder.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The active player chooses first even when the opponent controls Ruin Grinder")
    void activePlayerChoosesFirst() {
        harness.forceActivePlayer(player2);
        Card firstHand = new Mountain();
        Card secondHand = new Forest();
        harness.setHand(player1, List.of(firstHand));
        harness.setHand(player2, List.of(secondHand));

        Permanent grinder = harness.addToBattlefieldAndReturn(player1, new RuinGrinder());
        grinder.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstHand);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondHand);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mountaincycling puts the selected Mountain into hand without drawing a card")
    void mountaincyclingCompletesSearch() {
        Card grinder = new RuinGrinder();
        Card mountain = new Mountain();
        Card forest = new Forest();
        harness.setHand(player1, List.of(grinder));
        harness.setLibrary(player1, List.of(forest, mountain));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(grinder);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mountaincycling can fail to find even when a Mountain is available")
    void mountaincyclingCanFailToFind() {
        Card mountain = new Mountain();
        harness.setHand(player1, List.of(new RuinGrinder()));
        harness.setLibrary(player1, List.of(mountain));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
        harness.assertInGraveyard(player1, "Ruin Grinder");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("All hands are discarded before Notion Thief redirects the draws")
    void stolenDrawsAreNotDiscardedByLaterPlayer() {
        Card firstHand = new Mountain();
        Card secondHand = new Forest();
        harness.setHand(player1, List.of(firstHand));
        harness.setHand(player2, List.of(secondHand));
        harness.setLibrary(player1, List.of(new Mountain()));
        List<Card> stolenAndOwnDraws = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            stolenAndOwnDraws.add(new Mountain());
        }
        harness.setLibrary(player2, stolenAndOwnDraws);
        harness.addToBattlefield(player2, new NotionThief());

        Permanent grinder = harness.addToBattlefieldAndReturn(player1, new RuinGrinder());
        grinder.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(stolenAndOwnDraws);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondHand);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void fillLibrary(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        harness.setLibrary(player, cards);
    }
}
