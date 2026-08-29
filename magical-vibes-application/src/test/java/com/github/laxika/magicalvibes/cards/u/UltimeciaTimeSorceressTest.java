package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UltimeciaTimeSorceress.class, UltimeciaOmnipotent.class, GrizzlyBears.class})
class UltimeciaTimeSorceressTest extends BaseCardTest {

    @Test
    void entersAndAttacksWithSurveilTwo() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new UltimeciaTimeSorceress()));
        addManaForUltimecia();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry enterSurveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(enterSurveil).isNotNull();
        assertThat(enterSurveil.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        Card attackFirst = new GrizzlyBears();
        Card attackSecond = new GrizzlyBears();
        harness.setLibrary(player1, List.of(attackFirst, attackSecond));
        Permanent ultimecia = findPermanent(player1, "Ultimecia, Time Sorceress");
        ultimecia.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ultimecia)));
        resolveAllTriggers();

        PendingInteraction.Scry attackSurveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(attackSurveil).isNotNull();
        assertThat(attackSurveil.cards()).containsExactly(attackFirst, attackSecond);
    }

    @Test
    void paysAndExilesEightCardsToTransformAndTakeAnExtraTurn() {
        Permanent ultimecia = addUltimecia(player1);
        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            graveyard.add(new GrizzlyBears());
        }
        harness.setGraveyard(player1, graveyard);

        advanceToEndStep(player1);
        addManaForUltimecia();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(ultimecia.isTransformed()).isTrue();
        assertThat(ultimecia.getCard()).isInstanceOf(UltimeciaOmnipotent.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    void doesNotOfferTransformWithoutEightGraveyardCards() {
        Permanent ultimecia = addUltimecia(player1);
        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            graveyard.add(new GrizzlyBears());
        }
        harness.setGraveyard(player1, graveyard);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(ultimecia.isTransformed()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
    }

    private Permanent addUltimecia(Player player) {
        Permanent ultimecia = harness.addToBattlefieldAndReturn(player, new UltimeciaTimeSorceress());
        ultimecia.setSummoningSick(false);
        return ultimecia;
    }

    private void addManaForUltimecia() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
