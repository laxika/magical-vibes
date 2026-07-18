package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MycosynthWellspringTest extends BaseCardTest {

    // ===== ETB trigger =====

    @Test
    @DisplayName("Casting Mycosynth Wellspring puts it on stack as artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MycosynthWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mycosynth Wellspring");
    }

    @Test
    @DisplayName("Resolving artifact spell puts ETB trigger on stack with may prompt")
    void resolvingPutsEtbOnStack() {
        harness.setHand(player1, List.of(new MycosynthWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell -> artifact enters, MayEffect on stack
        harness.assertOnBattlefield(player1, "Mycosynth Wellspring");

        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting ETB may prompt allows choosing a basic land from library")
    void acceptingEtbMayAllowsChoosingBasicLand() {
        setupLibraryWithBasicLands();
        harness.setHand(player1, List.of(new MycosynthWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell -> artifact enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline -> library search

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getSupertypes().contains(com.github.laxika.magicalvibes.model.CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Choosing a basic land from ETB search puts it into hand")
    void choosingBasicLandFromEtbPutsIntoHand() {
        setupLibraryWithBasicLands();
        harness.setHand(player1, List.of(new MycosynthWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell -> artifact enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline -> library search

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining ETB may prompt skips the library search")
    void decliningEtbMaySkipsSearch() {
        setupLibraryWithBasicLands();
        harness.setHand(player1, List.of(new MycosynthWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell -> artifact enters, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(entry -> entry.contains("searches their library"));
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("Destroying Mycosynth Wellspring triggers may-search for basic land")
    void deathTriggerSearchesForBasicLand() {
        setupLibraryWithBasicLands();
        harness.addToBattlefield(player1, new MycosynthWellspring());

        // Use Shatter to destroy the Wellspring
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        var targetId = harness.getPermanentId(player1, "Mycosynth Wellspring");
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities(); // resolve Shatter — destroys Wellspring, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt

        // Death trigger should present may prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline -> library search

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Mycosynth Wellspring");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }

    @Test
    @DisplayName("Declining death trigger may prompt skips library search")
    void decliningDeathMaySkipsSearch() {
        harness.addToBattlefield(player1, new MycosynthWellspring());

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        var targetId = harness.getPermanentId(player1, "Mycosynth Wellspring");
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities(); // resolve Shatter — destroys Wellspring, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Mycosynth Wellspring");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(entry -> entry.contains("searches their library"));
    }

    private void setupLibraryWithBasicLands() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
