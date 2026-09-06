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

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
