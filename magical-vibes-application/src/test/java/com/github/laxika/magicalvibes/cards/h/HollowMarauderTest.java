package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LibraryOfLeng;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HollowMarauder.class, GrizzlyBears.class, HillGiant.class, Mountain.class, LibraryOfLeng.class})
class HollowMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when the targeted opponent discards a card with mana value less than four")
    void drawsForLowManaValueDiscard() {
        Mountain drawn = new Mountain();
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw when the targeted opponent discards a card with mana value four")
    void doesNotDrawForHighManaValueDiscard() {
        Mountain drawn = new Mountain();
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.setHand(player2, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId()));
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Draws when the targeted opponent has no card to discard")
    void drawsWhenOpponentHasNoCards() {
        Mountain drawn = new Mountain();
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Reduces its cost for each creature card in its controller's graveyard")
    void reducesCostForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hollow Marauder");
    }

    @Test
    @DisplayName("Only opponents can be targeted")
    void onlyOpponentsCanBeTargeted() {
        harness.setHand(player1, List.of(new HollowMarauder()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void choosingNoOpponentsDoesNotDiscardOrDraw() {
        Mountain drawn = new Mountain();
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castCreature(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hollow Marauder");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void noncreaturesAndOpponentsGraveyardsDoNotReduceCost() {
        harness.setGraveyard(player1, List.of(new Mountain()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void excessCreatureCardsReduceOnlyGenericMana() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hollow Marauder");
    }

    @Test
    void drawsWhenHighManaValueDiscardGoesToHiddenLibrary() {
        Mountain drawn = new Mountain();
        HillGiant discarded = new HillGiant();
        harness.addToBattlefield(player2, new LibraryOfLeng());
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(new Mountain()));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId()));
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void opponentsChooseBeforeAnyCardsAreDiscarded() {
        Player player3 = new Player(UUID.randomUUID(), "Third opponent");
        gd.playerIds.add(player3.getId());
        gd.orderedPlayerIds.add(player3.getId());
        gd.playerNames.add(player3.getUsername());
        gd.playerIdToName.put(player3.getId(), player3.getUsername());
        gd.playerDecks.put(player3.getId(), new ArrayList<>());
        gd.playerHands.put(player3.getId(), new ArrayList<>());
        gd.playerGraveyards.put(player3.getId(), new ArrayList<>());
        gd.playerBattlefields.put(player3.getId(), new ArrayList<>());
        gd.playerManaPools.put(player3.getId(), new ManaPool());
        gd.playerLifeTotals.put(player3.getId(), 20);
        GrizzlyBears firstDiscard = new GrizzlyBears();
        HillGiant secondDiscard = new HillGiant();
        Mountain drawn = new Mountain();
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.setHand(player2, List.of(firstDiscard));
        harness.setHand(player3, List.of(secondDiscard));
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId(), player3.getId()));
        List<Player> players = List.of(player1, player2, player3);
        for (int passes = 0; passes < 12 && !gd.interaction.isAwaitingInput() && !gd.stack.isEmpty(); passes++) {
            UUID priorityId = gqs.getPriorityPlayerId(gd);
            Player priorityPlayer = players.stream().filter(p -> p.getId().equals(priorityId)).findFirst().orElseThrow();
            gs.passPriority(gd, priorityPlayer);
        }
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(firstDiscard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player3, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player3, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
