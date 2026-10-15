package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.p.Phyresis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MitoticManipulation.class, GrizzlyBears.class, LlanowarElves.class, Shock.class, Plains.class, Swamp.class, Phyresis.class})
class MitoticManipulationTest extends BaseCardTest {

    

    @Test
    @DisplayName("Resolves by offering only cards matching permanent names on battlefield")
    void resolvesOfferingOnlyMatchingCards() {
        // Put a Grizzly Bears on the battlefield
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Set top 7 of library: only the second Grizzly Bears matches a permanent name
        harness.setLibrary(player1, List.of(
                new LlanowarElves(),
                new GrizzlyBears(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        // Only the Grizzly Bears should be offered
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing a matching card puts it onto the battlefield then orders rest on bottom")
    void choosingMatchingCardPutsOnBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        Plains plains = new Plains();
        Swamp swamp = new Swamp();
        Shock shock2 = new Shock();
        Plains plains2 = new Plains();
        harness.setLibrary(player1, List.of(bears, elves, shock, plains, swamp, shock2, plains2));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // Choose the Grizzly Bears
        harness.handleCardChosen(player1, 0);

        // Grizzly Bears should be on the battlefield
        long bearsCount = countPermanents(player1, "Grizzly Bears");
        assertThat(bearsCount).isEqualTo(2); // original + newly placed

        // Should be reordering the remaining 6 cards
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(6);
    }

    @Test
    @DisplayName("May choose not to put a card onto the battlefield")
    void mayDeclineToChoose() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setLibrary(player1, List.of(
                new GrizzlyBears(),
                new LlanowarElves(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        // No new permanent should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        // All 7 cards should be reordered to bottom
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(7);
    }

    @Test
    @DisplayName("No matching cards means all are reordered to bottom")
    void noMatchingCardsReordersAllToBottom() {
        // Battlefield has only a Grizzly Bears, but top 7 has no Grizzly Bears
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setLibrary(player1, List.of(
                new LlanowarElves(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains(),
                new Swamp()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // No matching cards â€” go directly to reorder
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(7);
    }

    @Test
    @DisplayName("Considers permanents on all players' battlefields")
    void considersAllPlayersBattlefields() {
        // Opponent has Llanowar Elves on battlefield, player1 has no permanents
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setLibrary(player1, List.of(
                new LlanowarElves(),
                new GrizzlyBears(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Llanowar Elves matches the opponent's permanent
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName()).isEqualTo("Llanowar Elves");
    }

    @Test
    @DisplayName("Multiple matching cards are all offered for selection")
    void multipleMatchingCardsAreAllOffered() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        harness.setLibrary(player1, List.of(
                new GrizzlyBears(),
                new LlanowarElves(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new GrizzlyBears(),
                new LlanowarElves()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // All 4 matching cards should be offered
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Grizzly Bears", "Grizzly Bears", "Llanowar Elves", "Llanowar Elves");
    }

    @Test
    @DisplayName("With empty library, Mitotic Manipulation does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Mitotic Manipulation goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setLibrary(player1, List.of(
                new GrizzlyBears(),
                new LlanowarElves(),
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // The spell only reaches the graveyard once its resolution finishes
        harness.handleCardChosen(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3, 4, 5)));

        harness.assertInGraveyard(player1, "Mitotic Manipulation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a card then completing reorder puts rest on bottom of library")
    void fullFlowWithReorder() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(bears, elves, shock, new Plains(), new Swamp(), new Shock(), new Plains()));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // Choose the Grizzly Bears
        harness.handleCardChosen(player1, 0);

        // Now reorder the remaining 6 cards
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(remaining).hasSize(6);

        // Reorder in original order (0,1,2,3,4,5)
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3, 4, 5)));

        // Library should have 6 cards on bottom
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        // No more awaiting input
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A matching land enters under the caster's control and the rest retain the chosen bottom order")
    void matchingLandAndBottomOrder() {
        harness.addToBattlefield(player2, new Plains());
        Plains chosen = new Plains();
        Card first = new Shock();
        Card second = new Swamp();
        Card third = new LlanowarElves();
        Card fourth = new Shock();
        Card fifth = new Swamp();
        Card sixth = new GrizzlyBears();
        Card untouched = new Plains();
        harness.setLibrary(player1, List.of(chosen, first, second, third, fourth, fifth, sixth, untouched));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(chosen);
        harness.handleCardChosen(player1, 0);
        harness.getGameService().handleInteractionAnswer(harness.getGameData(), player1,
                new InteractionAnswer.CardOrder(List.of(5, 4, 3, 2, 1, 0)));

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(chosen);
                    assertThat(permanent.isTapped()).isFalse();
                });
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactly(untouched, sixth, fifth, fourth, third, second, first);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library shorter than seven permits declining and ordering every card")
    void shortLibraryMayDeclineAndReorder() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(bears, shock, plains));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);
        harness.getGameService().handleInteractionAnswer(harness.getGameData(), player1,
                new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(plains, bears, shock);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Mitotic Manipulation");
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A matching Aura enters attached to a legally chosen creature")
    void matchingAuraChoosesAttachment() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        var host = findPermanent(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new Phyresis());
        findPermanent(player2, "Phyresis").setAttachedTo(findPermanent(player2, "Llanowar Elves").getId());
        Phyresis chosen = new Phyresis();
        harness.setLibrary(player1, List.of(chosen, new Shock()));
        harness.setHand(player1, List.of(new MitoticManipulation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, host.getId());
        assertThat(findPermanent(player1, "Phyresis").getAttachedTo()).isEqualTo(host.getId());
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Shock");
        harness.assertInGraveyard(player1, "Mitotic Manipulation");
    }
}