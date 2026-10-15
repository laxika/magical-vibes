package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HeatedDebate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderingArchaic.class, Forest.class, Shock.class, HeatedDebate.class})
class WanderingArchaicTest extends BaseCardTest {

    @Test
    void frontFaceCastsAsCreature() {
        harness.setHand(player1, List.of(new WanderingArchaic()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wandering Archaic");
    }

    @Test
    void backFaceLetsEachPlayerTakeAlandAndGainLife() {
        harness.setHand(player1, List.of(new WanderingArchaic()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch firstSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch)
                .as("stack=%s pending=%s", gd.stack, gd.pendingEffectResolutionEntry)
                .isNotNull();
        assertThat(firstSearch.params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }

    @Test
    void opponentMayPayToPreventCopyingTheirInstant() {
        harness.addToBattlefield(player1, new WanderingArchaic());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    void ifOpponentDoesNotPayControllerMayCopyTheInstant() {
        harness.addToBattlefield(player1, new WanderingArchaic());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(StackEntry::isCopy);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertLife(player1, 16);
    }

    @Test
    void controllerCanDeclineCopyAfterOpponentDeclinesPayment() {
        harness.addToBattlefield(player1, new WanderingArchaic());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanTargetOpponentAndResolvesBeforeOriginal() {
        harness.addToBattlefield(player1, new WanderingArchaic());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        resolveAllTriggers();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void controllersOwnInstantDoesNotTriggerCopying() {
        harness.addToBattlefield(player1, new WanderingArchaic());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsCreatureDoesNotTriggerCopying() {
        harness.addToBattlefield(player1, new WanderingArchaic());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WanderingArchaic()));
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castCreature(player2, 0, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Wandering Archaic");
    }

    @Test
    void backFaceCanTakeOneLandAndOneInstantButNotCardsBelowTopFive() {
        Forest land = new Forest();
        Shock instant = new Shock();
        HeatedDebate otherInstant = new HeatedDebate();
        WanderingArchaic creature1 = new WanderingArchaic();
        WanderingArchaic creature2 = new WanderingArchaic();
        Forest sixthCard = new Forest();
        harness.setHand(player1, List.of(new WanderingArchaic()));
        harness.setLibrary(player1, List.of(land, instant, otherInstant, creature1, creature2, sixthCard));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(land);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(instant, otherInstant);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                sixthCard, otherInstant, creature1, creature2);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(sixthCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }

    @Test
    void backFaceCanDeclineBothEligibleCardsAndStillGainLife() {
        Forest land = new Forest();
        Shock instant = new Shock();
        harness.setHand(player1, List.of(new WanderingArchaic()));
        harness.setLibrary(player1, List.of(land, instant));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, instant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }

    @Test
    void backFaceWithNoEligibleCardsRandomlyBottomsThemWithoutAnOrderingChoice() {
        List<WanderingArchaic> creatures = List.of(new WanderingArchaic(), new WanderingArchaic(),
                new WanderingArchaic(), new WanderingArchaic(), new WanderingArchaic());
        harness.setHand(player1, List.of(new WanderingArchaic()));
        harness.setLibrary(player1, List.copyOf(creatures));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(creatures);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }

    @Test
    void backFaceRevealsCardsStartingWithActivePlayer() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WanderingArchaic()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player2, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }

    @Test
    void backFaceCanTakeAnInstantWithoutALand() {
        Shock instant = new Shock();
        harness.setHand(player1, List.of(new WanderingArchaic()));
        harness.setLibrary(player1, List.of(instant));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }

    @Test
    void opponentCastingSorceryAlsoOffersPaymentAndCopy() {
        harness.addToBattlefield(player1, new WanderingArchaic());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WanderingArchaic()));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player2, 0, 1, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 26);
        harness.assertLife(player2, 26);
        harness.assertNotOnBattlefield(player2, "Wandering Archaic");
    }
}
