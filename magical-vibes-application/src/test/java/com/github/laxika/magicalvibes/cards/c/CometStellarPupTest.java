package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CometStellarPup.class, GrizzlyBears.class})
class CometStellarPupTest extends BaseCardTest {

    @Test
    @DisplayName("A low roll removes one loyalty and returns a qualifying graveyard card")
    void lowRollReturnsLowManaValueCard() {
        Permanent comet = addComet(1000);
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        Permanent target = addHighToughnessCreature();

        boolean observed = false;
        for (int i = 0; i < 60 && !observed; i++) {
            int loyaltyBefore = comet.getCounterCount(CounterType.LOYALTY);
            harness.setGraveyard(player1, List.of(returned));
            activateAndResolve(comet, target);
            if (gd.playerHands.get(player1.getId()).contains(returned)) {
                observed = true;
                assertThat(comet.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 1);
                assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(returned);
            }
            comet.setLoyaltyActivationsThisTurn(0);
        }

        assertThat(observed).isTrue();
    }

    @Test
    @DisplayName("A high roll grants two additional loyalty activations")
    void highRollGrantsTwoExtraActivations() {
        Permanent comet = addComet(1000);
        Permanent target = addHighToughnessCreature();

        boolean observed = false;
        for (int i = 0; i < 60 && !observed; i++) {
            int extraBefore = comet.getExtraLoyaltyActivationsThisTurn();
            activateAndResolve(comet, target);
            observed = comet.getExtraLoyaltyActivationsThisTurn() >= extraBefore + 2;
            comet.setLoyaltyActivationsThisTurn(0);
        }

        assertThat(observed).isTrue();
    }

    private Permanent addComet(int loyalty) {
        Permanent comet = new Permanent(new CometStellarPup());
        comet.setCounterCount(CounterType.LOYALTY, loyalty);
        comet.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(comet);
        return comet;
    }

    private Permanent addHighToughnessCreature() {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setPower(10000);
        card.setToughness(10000);
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        return creature;
    }

    private void activateAndResolve(Permanent comet, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction pending = gd.interaction.activeInteraction();
        if (pending instanceof PendingInteraction.GraveyardChoice choice) {
            harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());
        } else if (pending instanceof PendingInteraction.PermanentChoice choice) {
            assertThat(choice.validPermanentIds()).contains(target.getId());
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        }
    }
}
