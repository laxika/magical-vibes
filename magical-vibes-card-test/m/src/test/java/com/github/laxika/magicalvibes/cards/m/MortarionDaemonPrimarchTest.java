package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MortarionDaemonPrimarch.class)
class MortarionDaemonPrimarchTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, X is capped at life lost this turn and creates black Astartes Warrior tokens")
    void paysUpToLifeLostAndCreatesTokens() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);

        List<Permanent> tokens = findPermanents(player1, "Astartes Warrior");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes())
                    .contains(CardSubtype.ASTARTES, CardSubtype.WARRIOR);
            assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        });
    }

    @Test
    @DisplayName("Does not prompt when the controller has lost no life this turn")
    void noLifeLostDoesNotPrompt() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Astartes Warrior")).isEmpty();
    }

    @Test
    void canDeclinePaymentDespiteHavingManaAndLosingLife() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());
        gd.lifeLostThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(findPermanents(player1, "Astartes Warrior")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canChooseLessThanLifeLostAndPaysExactlyChosenAmount() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());
        gd.lifeLostThisTurn.put(player1.getId(), 4);

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(findPermanents(player1, "Astartes Warrior")).singleElement().satisfies(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        });
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void availableManaAlsoLimitsX() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());
        gd.lifeLostThisTurn.put(player1.getId(), 5);

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue())
                .isEqualTo(2);
        harness.handleXValueChosen(player1, 2);

        assertThat(findPermanents(player1, "Astartes Warrior")).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void opponentsLifeLossDoesNotIncreaseX() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());
        gd.lifeLostThisTurn.put(player2.getId(), 5);

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Astartes Warrior")).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());
        gd.lifeLostThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Astartes Warrior")).isEmpty();
    }

    @Test
    void lifeLostAfterTriggeringIsIncludedAtResolution() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 3, "life loss before trigger resolution"));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue())
                .isEqualTo(3);
        harness.handleXValueChosen(player1, 3);

        assertThat(findPermanents(player1, "Astartes Warrior")).hasSize(3);
    }

    @Test
    void lifePaymentsCountAndLifeGainDoesNotReduceX() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyLifePayment(gd, player1.getId(), 3, "life payment");
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3);
        });

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue())
                .isEqualTo(3);
        harness.handleXValueChosen(player1, 3);

        assertThat(findPermanents(player1, "Astartes Warrior")).hasSize(3);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
