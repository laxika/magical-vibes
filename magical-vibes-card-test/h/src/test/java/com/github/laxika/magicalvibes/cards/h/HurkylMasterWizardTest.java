package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AnkhOfMishra;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.c.CombatCourier;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HurkylMasterWizard.class, AnkhOfMishra.class, ChromaticStar.class,
        Divination.class, Forest.class, GrizzlyBears.class, Shock.class,
        CombatCourier.class, EnergyRefractor.class})
class HurkylMasterWizardTest extends BaseCardTest {

    @Test
    @DisplayName("At the end step, reveals one card for each noncreature spell card type")
    void revealsOneCardForEachNoncreatureSpellCardType() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new ChromaticStar()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new HurkylMasterWizard());
        List<Card> revealed = List.of(new Shock(), new AnkhOfMishra(), new GrizzlyBears(),
                new Forest(), new Divination());
        harness.setLibrary(player1, revealed);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch firstSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch.params().cards()).containsExactly(revealed.getFirst());
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch secondSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch.params().cards()).containsExactly(revealed.get(1));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(revealed.get(0), revealed.get(1));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(
                revealed.subList(2, revealed.size()));
    }

    @Test
    @DisplayName("Does not trigger when only a creature spell was cast")
    void doesNotTriggerForCreatureOnlyTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new HurkylMasterWizard());
        harness.setLibrary(player1, List.of(new Shock(), new AnkhOfMishra(),
                new GrizzlyBears(), new Forest(), new Divination()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void mayDeclineInstantAndStillChooseArtifactWithoutShufflingUnrevealedCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new ChromaticStar()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new HurkylMasterWizard());
        Card instant = new Shock();
        Card artifact = new ChromaticStar();
        List<Card> rest = List.of(instant, new Forest(), new Forest(), new Forest());
        Card sixth = new Divination();
        Card seventh = new GrizzlyBears();
        harness.setLibrary(player1, List.of(instant, artifact, rest.get(1), rest.get(2),
                rest.get(3), sixth, seventh));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        PendingInteraction.LibrarySearch artifactChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(artifactChoice.params().cards()).containsExactly(artifact);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 6))
                .containsExactlyInAnyOrderElementsOf(rest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void artifactCreatureMayBeChosenFromShortLibraryAfterCastingNoncreatureArtifact() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new HurkylMasterWizard());
        Card courier = new CombatCourier();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(courier, land));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(courier);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(courier);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void castingArtifactCreatureDoesNotEnableTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CombatCourier()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new HurkylMasterWizard());
        List<Card> library = List.of(new EnergyRefractor(), new Forest());
        harness.setLibrary(player1, library);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void multipleSpellsOfSameTypeStillAllowOnlyOneCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new HurkylMasterWizard());
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDeclineOnlyEligibleCardAndBottomAllRevealedCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new HurkylMasterWizard());
        List<Card> revealed = List.of(new Shock(), new Forest());
        harness.setLibrary(player1, revealed);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(revealed);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noMatchingRevealedCardsRequiresNoChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new HurkylMasterWizard());
        List<Card> revealed = List.of(new Forest(), new CombatCourier());
        harness.setLibrary(player1, revealed);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(revealed);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
