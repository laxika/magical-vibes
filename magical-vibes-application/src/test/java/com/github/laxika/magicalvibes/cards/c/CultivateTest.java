package com.github.laxika.magicalvibes.cards.c;


import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RootboundCrag;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cultivate.class, Forest.class, Island.class, Plains.class, RuneclawBear.class, RootboundCrag.class, CosisTrickster.class})
class CultivateTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Cultivate puts it on the stack as a sorcery")
    void castingPutsOnStack() {
        Cultivate cultivate = new Cultivate();
        harness.setHand(player1, List.of(cultivate));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(cultivate);
    }

    @Test
    @DisplayName("Resolving Cultivate presents basic lands for battlefield tapped pick")
    void resolvingPresentsBasicLandsForBattlefieldTapped() {
        setupAndCast();
        setupLibraryWithMultipleBasicLands();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().followUp().basicLandToHand()).isNotNull();
    }

    @Test
    @DisplayName("Picking first card puts it onto battlefield tapped, then presents hand pick")
    void firstPickToBattlefieldThenHandSearch() {
        setupAndCast();
        setupLibraryWithMultipleBasicLands();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Pick first basic land for battlefield tapped
        harness.handleCardChosen(player1, 0);

        // First card enters battlefield tapped
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());

        // Second search begins for hand, with the follow-up consumed
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.HAND);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().followUp().basicLandToHand()).isNull();
    }

    @Test
    @DisplayName("Picking second card puts it into hand and shuffles library")
    void secondPickToHand() {
        setupAndCast();
        setupLibraryWithMultipleBasicLands();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        // First pick: battlefield tapped
        harness.handleCardChosen(player1, 0);
        // Second pick: hand
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Failing to find on battlefield pick ends the spell entirely (per ruling)")
    void failToFindOnBattlefieldPickEndsSpell() {
        setupAndCast();
        setupLibraryWithMultipleBasicLands();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Decline first pick — per ruling, finding only one card = BF tapped,
        // so declining the BF pick means finding zero. No hand pick offered.
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With only one basic land, picking it for battlefield leaves no hand pick")
    void oneBasicLandPickedForBattlefield() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new RuneclawBear(), new RuneclawBear()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Pick the only basic land for battlefield
        harness.handleCardChosen(player1, 0);

        // No more basic lands for hand search — should finish
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("With only one basic land, skipping battlefield pick ends the spell (per ruling)")
    void oneBasicLandSkipBattlefieldPickEndsSpell() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new RuneclawBear(), new RuneclawBear()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Decline battlefield pick — per ruling, if you find only one it must go
        // to the battlefield tapped, so declining means finding zero.
        harness.handleCardChosen(player1, -1);

        // Spell finishes — no hand pick offered
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Picking battlefield land then declining hand pick is valid (find one)")
    void pickBattlefieldThenDeclineHand() {
        setupAndCast();
        setupLibraryWithMultipleBasicLands();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Pick for battlefield
        harness.handleCardChosen(player1, 0);
        // Decline hand pick
        harness.handleCardChosen(player1, -1);

        // One land on battlefield tapped, no new cards in hand
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No basic lands in library logs and shuffles without prompting")
    void noBasicLandsNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no basic land cards"));
    }

    @Test
    @DisplayName("Empty library logs without prompting")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("it is empty"));
    }

    @Test
    @DisplayName("Chosen lands are revealed, moved to their chosen destinations, and removed from the library")
    void chosenLandsAreRevealedAndMoved() {
        Forest forest = new Forest();
        Island island = new Island();
        Plains plains = new Plains();
        RootboundCrag nonbasic = new RootboundCrag();
        RuneclawBear bear = new RuneclawBear();
        harness.setLibrary(player1, List.of(forest, island, plains, nonbasic, bear));
        setupAndCast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest, island, plains);
        harness.handleCardChosen(player1, 1);
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest, plains);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, nonbasic, bear);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals Island"))
                .anyMatch(entry -> entry.contains("reveals Plains"))
                .anyMatch(entry -> entry.contains("Library is shuffled"));
        harness.assertInGraveyard(player1, "Cultivate");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cultivate still triggers an opponent's shuffle ability with an empty library")
    void emptyLibraryStillTriggersShuffleAbility() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        harness.setLibrary(player1, List.of());
        setupAndCast();
        harness.passBothPriorities();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new Cultivate()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibraryWithMultipleBasicLands() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new RuneclawBear()));
    }
}
