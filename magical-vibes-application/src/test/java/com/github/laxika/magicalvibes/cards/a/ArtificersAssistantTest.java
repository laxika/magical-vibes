package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TheAntiquitiesWar;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArtificersAssistant.class, Spellbook.class, AdelizTheCinderWind.class,
        GrizzlyBears.class, TheAntiquitiesWar.class})
class ArtificersAssistantTest extends BaseCardTest {


    @Test
    @DisplayName("Casting an artifact spell triggers scry 1")
    void artifactSpellTriggersScry() {
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        // Spellbook on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Artificer's Assistant"));
    }

    @Test
    @DisplayName("Resolving artifact-triggered scry enters scry state")
    void artifactTriggerResolvesIntoScryState() {
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        // Resolve the triggered ability (LIFO — trigger on top, Spellbook below)
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }


    @Test
    @DisplayName("Casting a legendary creature triggers scry 1")
    void legendarySpellTriggersScry() {
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Adeliz on stack + triggered ability from Artificer's Assistant
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Artificer's Assistant"));
    }


    @Test
    @DisplayName("Casting a non-historic creature does not trigger scry")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }


    @Test
    @DisplayName("Opponent casting an artifact does not trigger controller's Artificer's Assistant")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArtificersAssistant());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        // Only the artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }


    @Test
    @DisplayName("Casting two artifact spells triggers scry 1 each time")
    void multipleHistoricSpellsTriggerMultipleTimes() {
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        // Cast first artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability (scry)
        harness.getGameService().handleInteractionAnswer(harness.getGameData(), player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities(); // resolve Spellbook

        // Cast second artifact
        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Artificer's Assistant"));
    }

    @Test
    @DisplayName("Casting a nonlegendary Saga triggers scry before the Saga resolves")
    void sagaSpellTriggersScry() {
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.setHand(player1, List.of(new TheAntiquitiesWar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        GrizzlyBears top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        harness.assertNotOnBattlefield(player1, "The Antiquities War");
    }

    @Test
    @DisplayName("Scry can leave the top card on top without drawing it")
    void scryKeepsTopCard() {
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.setHand(player1, List.of(new Spellbook()));
        GrizzlyBears top = new GrizzlyBears();
        AdelizTheCinderWind bottom = new AdelizTheCinderWind();
        harness.setLibrary(player1, List.of(top, bottom));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom without affecting the opponent's library")
    void scryBottomsTopCard() {
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.setHand(player1, List.of(new Spellbook()));
        GrizzlyBears top = new GrizzlyBears();
        AdelizTheCinderWind second = new AdelizTheCinderWind();
        Spellbook opponentTop = new Spellbook();
        harness.setLibrary(player1, List.of(top, second));
        harness.setLibrary(player2, List.of(opponentTop));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Scry with an empty library completes without a choice")
    void emptyLibraryScryCompletes() {
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.setLibrary(player1, List.of());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Each Assistant triggers separately for one historic spell")
    void multipleAssistantsScrySeparately() {
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.addToBattlefield(player1, new ArtificersAssistant());
        harness.setHand(player1, List.of(new Spellbook()));
        GrizzlyBears first = new GrizzlyBears();
        AdelizTheCinderWind second = new AdelizTheCinderWind();
        harness.setLibrary(player1, List.of(first, second));

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.stack).hasSize(1);
    }
}
