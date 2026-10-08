package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbaddonTheDespoiler.class, GrizzlyBears.class, LlanowarElves.class, Mountain.class, Shock.class, Unsummon.class})
class AbaddonTheDespoilerTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, a hand spell within opponents' life loss cascades")
    void qualifyingHandSpellCascades() {
        setupAbaddon();
        dealTwoDamageToOpponent();

        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(new Mountain(), hit));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(hit);
    }

    @Test
    @DisplayName("A hand spell above the life-loss amount does not cascade")
    void spellAboveLifeLossDoesNotCascade() {
        setupAbaddon();

        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Abaddon grants cascade only during its controller's turn")
    void onlyDuringYourTurn() {
        setupAbaddon();
        dealTwoDamageToOpponent();
        harness.forceActivePlayer(player2);

        harness.setLibrary(player1, List.of(new Mountain(), new LlanowarElves()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Life lost before Abaddon enters still qualifies hand spells")
    void countsLifeLostBeforeEntering() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        dealTwoDamageToOpponent();
        harness.addToBattlefield(player1, new AbaddonTheDespoiler());

        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(hit);
    }

    @Test
    @DisplayName("The controller's own life loss does not grant cascade")
    void ownLifeLossDoesNotQualify() {
        setupAbaddon();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An already-triggered cascade resolves after Abaddon leaves")
    void cascadeSurvivesAbaddonLeaving() {
        setupAbaddon();
        dealTwoDamageToOpponent();

        LlanowarElves hit = new LlanowarElves();
        harness.setLibrary(player1, List.of(hit));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Abaddon the Despoiler"));
        harness.assertInHand(player1, "Abaddon the Despoiler");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(hit);
    }

    @Test
    @DisplayName("The cascade hit is cast for free from exile without gaining another cascade")
    void cascadeHitDoesNotReceiveHandOnlyGrant() {
        setupAbaddon();
        dealTwoDamageToOpponent();

        GrizzlyBears equalManaValue = new GrizzlyBears();
        LlanowarElves hit = new LlanowarElves();
        Mountain untouched = new Mountain();
        harness.setLibrary(player1, List.of(equalManaValue, hit, untouched));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(hit);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, equalManaValue);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAbaddon() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new AbaddonTheDespoiler());
    }

    private void dealTwoDamageToOpponent() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }
}
