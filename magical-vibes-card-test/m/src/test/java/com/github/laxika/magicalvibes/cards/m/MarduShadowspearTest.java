package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarduShadowspear.class})
class MarduShadowspearTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking causes each opponent to lose 1 life")
    void attackingCausesEachOpponentToLoseLife() {
        addCreatureReady(player1, new MarduShadowspear());

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("The attack trigger does not cause its controller to lose life")
    void attackTriggerDoesNotAffectController() {
        addCreatureReady(player1, new MarduShadowspear());

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new MarduShadowspear()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        Permanent shadowspear = findPermanent(player1, "Mardu Shadowspear");
        assertThat(shadowspear.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(shadowspear.getId())
                        && action.kind() == DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Mardu Shadowspear");
        harness.assertNotOnBattlefield(player1, "Mardu Shadowspear");
    }

    @Test
    @DisplayName("The attack trigger loses exactly one life before combat damage")
    void attackTriggerResolvesBeforeCombatDamage() {
        addCreatureReady(player1, new MarduShadowspear());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });

        harness.assertLife(player2, opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Casting for dash does not create an enters-the-battlefield trigger")
    void dashSchedulesReturnWithoutAnEtbTrigger() {
        harness.setHand(player1, List.of(new MarduShadowspear()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mardu Shadowspear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting normally does not return the creature at the end step")
    void normalCastStaysOnBattlefieldAtEndStep() {
        harness.castFromHand(player1, new MarduShadowspear(), "{B}");
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mardu Shadowspear");
        harness.assertNotInHand(player1, "Mardu Shadowspear");
    }

    @Test
    @DisplayName("The dashed creature remains on the battlefield until its end-step trigger resolves")
    void dashReturnUsesTheStackAtEndStep() {
        harness.setHand(player1, List.of(new MarduShadowspear()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Mardu Shadowspear");
        harness.assertNotInHand(player1, "Mardu Shadowspear");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertInHand(player1, "Mardu Shadowspear");
        harness.assertNotOnBattlefield(player1, "Mardu Shadowspear");
    }

}
