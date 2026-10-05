package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KozilekCompleated.class, GrizzlyBears.class, JaceBeleren.class, Boomerang.class})
class KozilekCompleatedTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Kozilek gives opponents poison and makes them discard down to two")
    void castingPoisonsAndDiscardsOpponents() {
        harness.setHand(player1, List.of(new KozilekCompleated()));
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Attacking makes the defending player sacrifice one permanent per poison counter")
    void annihinfectSacrificesPerDefendingPlayerPoisonCounter() {
        Permanent kozilek = addCreatureReady(player1, new KozilekCompleated());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kozilek)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void castingDoesNotDiscardWhenOpponentHasTwoCardsAndDoesNotPoisonController() {
        harness.setHand(player1, List.of(new KozilekCompleated(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            assertThat(findPermanents(player1, "Kozilek, Compleated")).isEmpty();
        });

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enteringWithoutCastingDoesNotPoisonOrDiscard() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new KozilekCompleated());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noPoisonMeansNoSacrificesEvenWhenAttackerControllerHasPoison() {
        addCreatureReady(player1, new KozilekCompleated());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerPoisonCounters.put(player1.getId(), 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bear);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void sacrificeCountUsesPoisonAtResolutionAndSacrificesAsManyAsPossible() {
        addCreatureReady(player1, new KozilekCompleated());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            gd.playerPoisonCounters.put(player2.getId(), 3);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void defenderCanChooseANoncreaturePermanentToSacrifice() {
        addCreatureReady(player1, new KozilekCompleated());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.handleMultiplePermanentsChosen(player2, List.of(jace.getId()));
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bear);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(jace.getCard());
    }

    @Test
    void annihinfectStillAffectsDefenderAfterAttackedPlaneswalkerLeaves() {
        addCreatureReady(player1, new KozilekCompleated());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, jace.getId()));
            harness.castInstant(player1, 0, jace.getId());
            harness.passBothPriorities();
            assertThat(gd.playerHands.get(player2.getId())).contains(jace.getCard());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
