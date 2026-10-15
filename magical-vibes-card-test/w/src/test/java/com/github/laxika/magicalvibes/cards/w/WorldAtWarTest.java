package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedAdditionalCombatBeginningEffect;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldAtWar.class, NestInvader.class})
class WorldAtWarTest extends BaseCardTest {

    @Test
    void untapsAttackedCreaturesAtBeginningOfAdditionalCombat() {
        Permanent attackedBear = addCreatureReady(player1, new NestInvader());
        Permanent nonAttackedBear = addCreatureReady(player1, new NestInvader());
        declareAttackers(List.of(0));
        nonAttackedBear.tap();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        WorldAtWar card = new WorldAtWar();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(attackedBear.isTapped()).isTrue();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof DelayedAdditionalCombatBeginningEffect);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(attackedBear.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(attackedBear.isTapped()).isFalse();
        assertThat(nonAttackedBear.isTapped()).isTrue();
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        WorldAtWar card = new WorldAtWar();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "World at War");
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void firstMainPhaseCastDoesNotTriggerUntappingInTheNormalCombat() {
        harness.setHand(player1, List.of(new WorldAtWar()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 0);

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    void castingDuringThirdMainPhaseAddsNoPhasesButStillRebounds() {
        WorldAtWar lateCast = new WorldAtWar();
        harness.setHand(player1, List.of(new WorldAtWar(), lateCast));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 0);

        gs.advanceStep(gd);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.findExiledCard(lateCast.getId())).isNotNull();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    void multipleCastsInSecondMainPhaseEachAddACombatAndMainPhase() {
        harness.setHand(player1, List.of(new WorldAtWar(), new WorldAtWar()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 10);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    void decliningReboundLeavesCardExiledWithoutAnotherOffer() {
        WorldAtWar card = new WorldAtWar();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
