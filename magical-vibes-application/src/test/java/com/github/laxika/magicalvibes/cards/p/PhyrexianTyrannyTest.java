package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianTyranny.class, CounselOfTheSoratami.class, Island.class})
class PhyrexianTyrannyTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may pay for each card drawn")
    void controllerMayPayForEachCardDrawn() {
        harness.addToBattlefield(player1, new PhyrexianTyranny());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The controller may decline to pay for each card drawn")
    void controllerMayDeclineForEachCardDrawn() {
        harness.addToBattlefield(player1, new PhyrexianTyranny());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An opponent loses life for every card drawn when they cannot pay")
    void opponentLosesLifeForEveryCardDrawnWithoutMana() {
        harness.addToBattlefield(player1, new PhyrexianTyranny());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An opponent can pay for one draw and lose life for the other")
    void opponentPaysForOnlyOneDraw() {
        harness.addToBattlefield(player1, new PhyrexianTyranny());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("All cards are drawn before any Tyranny payment or life loss")
    void drawsFinishBeforeTriggersResolve() {
        harness.addToBattlefield(player1, new PhyrexianTyranny());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(new CounselOfTheSoratami(), new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Untapped mana sources allow payment during trigger resolution")
    void offersPaymentWithUntappedLandsAndEmptyManaPool() {
        harness.addToBattlefield(player1, new PhyrexianTyranny());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
