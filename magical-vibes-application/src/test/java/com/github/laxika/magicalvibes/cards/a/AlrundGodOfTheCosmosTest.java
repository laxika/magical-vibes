package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Doomskar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HakkaWhisperingRaven;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlrundGodOfTheCosmos.class, HakkaWhisperingRaven.class, Doomskar.class,
        Forest.class, Island.class, GrizzlyBears.class})
class AlrundGodOfTheCosmosTest extends BaseCardTest {

    @Test
    void getsPowerAndToughnessForCardsInHandAndForetoldCardsInExile() {
        Permanent alrund = harness.addToBattlefieldAndReturn(player1, new AlrundGodOfTheCosmos());
        Doomskar foretold = new Doomskar();
        harness.setHand(player1, List.of(new Forest(), new Island(), new GrizzlyBears(), foretold));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.getEffectivePower(gd, alrund)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, alrund)).isEqualTo(5);

        harness.foretell(player1, 3);

        assertThat(gqs.getEffectivePower(gd, alrund)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, alrund)).isEqualTo(5);
    }

    @Test
    void choosesCardTypeAndPutsMatchingRevealedCardsIntoHand() {
        Permanent alrund = harness.addToBattlefieldAndReturn(player1, new AlrundGodOfTheCosmos());
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(alrund);
    }

    @Test
    void hakkaReturnsToHandAndScriesAfterCombatDamage() {
        AlrundGodOfTheCosmos card = new AlrundGodOfTheCosmos();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent hakka = findPermanent(player1, "Hakka, Whispering Raven");
        hakka.setSummoningSick(false);
        Card top = new Forest();
        Card second = new Island();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second, third));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hakka)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(returnedCard -> returnedCard.getId().equals(card.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second, third);
    }

    @Test
    void frontFaceCanBeCastForItsOwnManaCost() {
        AlrundGodOfTheCosmos card = new AlrundGodOfTheCosmos();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Alrund, God of the Cosmos").getOriginalCard()).isSameAs(card);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void nonmatchingCardsGoBelowUnrevealedCardsInChosenOrder() {
        harness.addToBattlefield(player1, new AlrundGodOfTheCosmos());
        Card first = new Forest();
        Card second = new Island();
        Card unrevealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, unrevealed));

        resolveEndStep(player1);
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, second, first);
    }

    @Test
    void allMatchingRevealedCardsGoToHandWithoutTakingUnrevealedCards() {
        harness.addToBattlefield(player1, new AlrundGodOfTheCosmos());
        Card first = new Forest();
        Card second = new Island();
        Card unrevealed = new Forest();
        harness.setLibrary(player1, List.of(first, second, unrevealed));

        resolveEndStep(player1);
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void oneCardLibraryRevealsOnlyTheAvailableCard() {
        harness.addToBattlefield(player1, new AlrundGodOfTheCosmos());
        Card only = new Forest();
        harness.setLibrary(player1, List.of(only));

        resolveEndStep(player1);
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryStillAllowsTypeChoiceAndFinishesResolution() {
        harness.addToBattlefield(player1, new AlrundGodOfTheCosmos());
        harness.setLibrary(player1, List.of());

        resolveEndStep(player1);
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new AlrundGodOfTheCosmos());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));

        resolveEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void excludesOrdinaryExiledCardsAndOpponentsForetoldCards() {
        Permanent alrund = harness.addToBattlefieldAndReturn(player1, new AlrundGodOfTheCosmos());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Doomskar()));
        harness.setExile(player1, List.of(new Doomskar()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.foretell(player2, 0);

        assertThat(gqs.getEffectivePower(gd, alrund)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, alrund)).isEqualTo(1);
    }

    @Test
    void bonusUpdatesWhenControllerHandChanges() {
        Permanent alrund = harness.addToBattlefieldAndReturn(player1, new AlrundGodOfTheCosmos());
        harness.setHand(player1, List.of(new Forest(), new Island()));
        harness.setHand(player2, List.of(new Forest(), new Island(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, alrund)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, alrund)).isEqualTo(3);

        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, alrund)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, alrund)).isEqualTo(1);
    }

    @Test
    void hakkaReturnsAsFrontFaceBeforeScryAndCanBottomACard() {
        AlrundGodOfTheCosmos card = new AlrundGodOfTheCosmos();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        Permanent hakka = findPermanent(player1, "Hakka, Whispering Raven");
        hakka.setSummoningSick(false);
        Card top = new Forest();
        Card second = new Island();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second, third));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hakka)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hakka);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, top);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
