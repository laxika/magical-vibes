package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfMisfortunesTest extends BaseCardTest {

    // ===== Upkeep trigger: search and attach =====

    @Test
    @DisplayName("At controller's upkeep, may search and attach a Curse to the enchanted player")
    void searchAttachesCurseToEnchantedPlayer() {
        placeCurseOnPlayer(player1, player2);
        setupLibrary(player1, List.of(curse("Fake Curse"), new GrizzlyBears()));

        advanceToControllerUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactly("Fake Curse");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent newCurse = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fake Curse"))
                .findFirst().orElseThrow();
        assertThat(newCurse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining the may trigger puts no Curse onto the battlefield")
    void decliningSearchesNothing() {
        placeCurseOnPlayer(player1, player2);
        setupLibrary(player1, List.of(curse("Fake Curse"), new GrizzlyBears()));

        advanceToControllerUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
    }

    // ===== Name exclusion =====

    @Test
    @DisplayName("Curse sharing a name with one already attached is excluded from the search")
    void excludesCurseWithSameNameAsAttached() {
        placeCurseOnPlayer(player1, player2); // Curse of Misfortunes attached to player2
        // An additional curse named "Shared" already attached to the enchanted player
        Permanent shared = new Permanent(curse("Shared"));
        shared.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(shared);

        setupLibrary(player1, List.of(curse("Shared"), curse("Unique")));

        advanceToControllerUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Only the differently-named curse may be searched for
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactly("Unique");
    }

    @Test
    @DisplayName("No eligible Curse in library — no library search is opened")
    void noEligibleCurseFindsNothing() {
        placeCurseOnPlayer(player1, player2);
        // Only a same-named curse as one already attached (Curse of Misfortunes) plus a non-curse
        setupLibrary(player1, List.of(new CurseOfMisfortunes(), new GrizzlyBears()));

        advanceToControllerUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no eligible Curse cards"));
    }

    // ===== Trigger timing =====

    @Test
    @DisplayName("Trigger does NOT fire during the enchanted player's upkeep")
    void triggerDoesNotFireDuringEnchantedPlayerUpkeep() {
        placeCurseOnPlayer(player1, player2);
        setupLibrary(player1, List.of(curse("Fake Curse"), new GrizzlyBears()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    // ===== Helpers =====

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = new Permanent(new CurseOfMisfortunes());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(cursePerm);
        return cursePerm;
    }

    private Card curse(String name) {
        GrizzlyBears fakeCurse = new GrizzlyBears();
        fakeCurse.setName(name);
        fakeCurse.setSubtypes(List.of(CardSubtype.AURA, CardSubtype.CURSE));
        fakeCurse.setType(CardType.ENCHANTMENT);
        return fakeCurse;
    }

    private void setupLibrary(Player player, List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void advanceToControllerUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance into upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve the trigger → "you may search" prompt
    }
}
