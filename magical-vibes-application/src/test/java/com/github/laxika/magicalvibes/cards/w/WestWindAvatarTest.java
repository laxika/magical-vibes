package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedStoneseeker;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WestWindAvatar.class, Forest.class, GrizzlyBears.class, HornedStoneseeker.class})
class WestWindAvatarTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void clearInitialHand() {
        harness.setHand(player1, List.of());
    }

    @Test
    @DisplayName("Its ETB trigger can sacrifice a land and gain 3 life")
    void etbSacrificesLandAndGainsLife() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new WestWindAvatar());

        resolveSacrificeChoice(forest);

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Its ETB trigger can sacrifice a token and gain 3 life")
    void etbSacrificesTokenAndGainsLife() {
        harness.enterBattlefieldAndReturn(player1, new HornedStoneseeker());
        harness.passBothPriorities();
        Permanent powerstone = findPermanent(player1, "Powerstone");
        harness.enterBattlefieldAndReturn(player1, new WestWindAvatar());

        resolveSacrificeChoice(powerstone);

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Powerstone");
    }

    @Test
    @DisplayName("Declining its ETB trigger does not sacrifice a permanent or gain life")
    void decliningEtbDoesNothing() {
        harness.addToBattlefield(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new WestWindAvatar());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Its attack trigger can sacrifice a land and gain 3 life")
    void attackSacrificesLandAndGainsLife() {
        Permanent avatar = addReadyAvatar();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(avatar)));
        resolveSacrificeChoice(forest);

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Disappear draws a card after your permanent leaves this turn")
    void disappearDrawsAfterPermanentLeaves() {
        harness.addToBattlefield(player1, new WestWindAvatar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        resolveEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears.getCard(), drawnCard);
    }

    @Test
    @DisplayName("Disappear does not draw a card without a permanent leaving this turn")
    void disappearDoesNotDrawWithoutPermanentLeaving() {
        harness.addToBattlefield(player1, new WestWindAvatar());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        resolveEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Accepting the ETB trigger without a token or land does not gain life")
    void acceptingWithoutEligiblePermanentDoesNotGainLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new WestWindAvatar());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "West Wind Avatar");
    }

    @Test
    @DisplayName("The sacrifice choice includes only your tokens and lands")
    void sacrificeChoiceExcludesOpposingLandsAndNontokenCreatures() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new WestWindAvatar());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(forest.getId());
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertLife(player1, 23);
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing a token to the ETB trigger enables Disappear")
    void sacrificedTokenEnablesDisappear() {
        harness.enterBattlefieldAndReturn(player1, new HornedStoneseeker());
        harness.passBothPriorities();
        Permanent powerstone = findPermanent(player1, "Powerstone");
        harness.enterBattlefieldAndReturn(player1, new WestWindAvatar());
        resolveSacrificeChoice(powerstone);
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        resolveEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Disappear ignores permanents that left under an opponent's control")
    void opponentPermanentLeavingDoesNotEnableDisappear() {
        harness.addToBattlefield(player1, new WestWindAvatar());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        resolveEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Disappear does not trigger during an opponent's end step")
    void disappearDoesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new WestWindAvatar());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("A permanent leaving after the end step begins does not trigger Disappear")
    void permanentLeavingAfterEndStepBeginsDoesNotTriggerDisappear() {
        harness.addToBattlefield(player1, new WestWindAvatar());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, forest));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Disappear draws only once even when multiple permanents left this turn")
    void multiplePermanentsLeavingDrawsOnlyOneCard() {
        harness.addToBattlefield(player1, new WestWindAvatar());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, firstForest);
            harness.getPermanentRemovalService().removePermanentToHand(gd, secondForest);
        });
        Forest drawnCard = new Forest();
        Forest remainingCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));

        resolveEndStepTrigger();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstForest.getCard(), secondForest.getCard(), drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    private void resolveSacrificeChoice(Permanent permanent) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, permanent.getId());
    }

    private Permanent addReadyAvatar() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new WestWindAvatar());
        avatar.setSummoningSick(false);
        return avatar;
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
