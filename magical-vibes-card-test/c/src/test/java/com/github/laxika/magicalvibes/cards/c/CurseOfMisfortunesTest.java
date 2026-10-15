package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfMisfortunes.class, CurseOfThirst.class, CurseOfExhaustion.class, BlackCat.class})
class CurseOfMisfortunesTest extends BaseCardTest {

    @Test
    @DisplayName("At controller's upkeep, may search and attach a Curse to the enchanted player")
    void searchAttachesCurseToEnchantedPlayer() {
        placeCurseOnPlayer(player1, player2);
        harness.setLibrary(player1, List.of(new CurseOfThirst(), new BlackCat()));

        advanceToControllerUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactly("Curse of Thirst");

        harness.handleCardChosen(player1, 0);

        Permanent newCurse = findPermanent(player1, "Curse of Thirst");
        assertThat(newCurse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining the may trigger puts no Curse onto the battlefield")
    void decliningSearchesNothing() {
        placeCurseOnPlayer(player1, player2);
        harness.setLibrary(player1, List.of(new CurseOfThirst(), new BlackCat()));

        advanceToControllerUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
    }

    @Test
    @DisplayName("Curse sharing a name with one already attached is excluded from the search")
    void excludesCurseWithSameNameAsAttached() {
        placeCurseOnPlayer(player1, player2); // Curse of Misfortunes attached to player2
        // A second Curse is already attached to the enchanted player.
        Permanent shared = harness.addToBattlefieldAndReturn(player1, new CurseOfExhaustion());
        shared.setAttachedTo(player2.getId());

        harness.setLibrary(player1, List.of(new CurseOfExhaustion(), new CurseOfThirst()));

        advanceToControllerUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Only the differently-named curse may be searched for
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactly("Curse of Thirst");
    }

    @Test
    @DisplayName("No eligible Curse in library — no library search is opened")
    void noEligibleCurseFindsNothing() {
        placeCurseOnPlayer(player1, player2);
        // Only a same-named curse as one already attached (Curse of Misfortunes) plus a non-curse
        harness.setLibrary(player1, List.of(new CurseOfMisfortunes(), new BlackCat()));

        advanceToControllerUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no eligible Curse cards"));
    }

    @Test
    @DisplayName("Trigger does NOT fire during the enchanted player's upkeep")
    void triggerDoesNotFireDuringEnchantedPlayerUpkeep() {
        placeCurseOnPlayer(player1, player2);
        harness.setLibrary(player1, List.of(new CurseOfThirst(), new BlackCat()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A resolved upkeep ability can fail to find an eligible Curse")
    void mayFailToFind() {
        placeCurseOnPlayer(player1, player2);
        harness.setLibrary(player1, List.of(new CurseOfThirst()));
        advanceToControllerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Curses controlled by the enchanted player also exclude matching names")
    void excludesNamesAcrossControllers() {
        placeCurseOnPlayer(player1, player2);
        Permanent attached = harness.addToBattlefieldAndReturn(player2, new CurseOfExhaustion());
        attached.setAttachedTo(player2.getId());
        harness.setLibrary(player1, List.of(new CurseOfExhaustion(), new CurseOfThirst()));
        advanceToControllerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName).containsExactly("Curse of Thirst");
    }

    @Test
    @DisplayName("Curses attached to another player do not exclude their names")
    void otherPlayerCursesDoNotExcludeNames() {
        placeCurseOnPlayer(player1, player2);
        Permanent attached = harness.addToBattlefieldAndReturn(player1, new CurseOfExhaustion());
        attached.setAttachedTo(player1.getId());
        harness.setLibrary(player1, List.of(new CurseOfExhaustion()));
        advanceToControllerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof CurseOfExhaustion)
                .extracting(Permanent::getAttachedTo).containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("The ability still searches after its source leaves the battlefield")
    void sourceLeavingDoesNotStopSearch() {
        Permanent source = placeCurseOnPlayer(player1, player2);
        harness.setLibrary(player1, List.of(new CurseOfThirst()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Curse of Thirst").getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The controller can be the enchanted player")
    void canAttachFetchedCurseToController() {
        placeCurseOnPlayer(player1, player1);
        harness.setLibrary(player1, List.of(new CurseOfExhaustion()));
        advanceToControllerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Curse of Exhaustion").getAttachedTo()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Searching an empty library finishes without putting a Curse onto the battlefield")
    void emptyLibraryFindsNothing() {
        placeCurseOnPlayer(player1, player2);
        harness.setLibrary(player1, List.of());
        advanceToControllerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = harness.addToBattlefieldAndReturn(controller, new CurseOfMisfortunes());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        return cursePerm;
    }

    private void advanceToControllerUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance into upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve the trigger → "you may search" prompt
    }
}
