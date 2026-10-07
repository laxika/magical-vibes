package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TivashGloomSummoner.class})
class TivashGloomSummonerTest extends BaseCardTest {

    @Test
    @DisplayName("May pay life gained this turn to create an X/X Demon token")
    void mayPayLifeToCreateDemonToken() {
        harness.addToBattlefield(player1, new TivashGloomSummoner());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        Permanent demon = findPermanents(player1, "Demon").getFirst();
        assertThat(demon.getCard().getPower()).isEqualTo(3);
        assertThat(demon.getCard().getToughness()).isEqualTo(3);
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.getCard().getSubtypes()).contains(CardSubtype.DEMON);
        assertThat(gqs.hasKeyword(gd, demon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the payment creates no Demon token")
    void canDeclinePayment() {
        harness.addToBattlefield(player1, new TivashGloomSummoner());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Demon")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger without life gain")
    void doesNotTriggerWithoutLifeGain() {
        harness.addToBattlefield(player1, new TivashGloomSummoner());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Demon")).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay more life than the controller has")
    void cannotPayMoreLifeThanAvailable() {
        harness.addToBattlefield(player1, new TivashGloomSummoner());
        harness.setLife(player1, 2);
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(2);
        assertThat(findPermanents(player1, "Demon")).isEmpty();
    }

    @Test
    void lifelinkCombatDamageFundsTheDemon() {
        addCreatureReady(player1, new TivashGloomSummoner());

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            declareAttackers(List.of(0));
            resolveCombat();
        });

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().getPower()).isEqualTo(4);
        assertThat(demon.getCard().getToughness()).isEqualTo(4);
    }

    @Test
    void countsAllLifeGainIncludingGainBeforeTivashEnteredAndIgnoresLifeLoss() {
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3);
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 4, "Life loss");
        });
        harness.addToBattlefield(player1, new TivashGloomSummoner());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    void includesLifeGainedAfterTriggeringBeforeResolution() {
        harness.addToBattlefield(player1, new TivashGloomSummoner());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    void lifeGainedOnlyAfterEndStepBeginsDoesNotTrigger() {
        harness.addToBattlefield(player1, new TivashGloomSummoner());

        advanceToEndStep(player1);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Demon")).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new TivashGloomSummoner());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Demon")).isEmpty();
    }

    @Test
    void opponentsLifeGainDoesNotEnableTheTrigger() {
        harness.addToBattlefield(player1, new TivashGloomSummoner());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Demon")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
