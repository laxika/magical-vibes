package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZellDincht.class, Forest.class})
class ZellDinchtTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each land its controller controls")
    void getsPowerForControlledLands() {
        Permanent zell = harness.addToBattlefieldAndReturn(player1, new ZellDincht());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, zell)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zell)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lets its controller play one additional land each turn")
    void grantsAdditionalLandPlay() {
        harness.addToBattlefield(player1, new ZellDincht());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("At the beginning of its controller's end step, returns a chosen land")
    void returnsLandAtControllerEndStep() {
        harness.addToBattlefield(player1, new ZellDincht());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(forest.getId());

        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertInHand(player1, "Forest");
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(forest.getId()));
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Returning a land immediately reduces Zell's power")
    void powerUpdatesWhenLandReturns() {
        Permanent zell = harness.addToBattlefieldAndReturn(player1, new ZellDincht());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, zell)).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forest.getId());

        assertThat(gqs.getEffectivePower(gd, zell)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void doesNotReturnLandOnOpponentsTurn() {
        harness.addToBattlefield(player1, new ZellDincht());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Returns a controlled land to its owner rather than its controller")
    void returnsLandToOwner() {
        harness.addToBattlefield(player1, new ZellDincht());
        Forest forestCard = new Forest();
        forestCard.setOwnerId(player2.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, forestCard);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The end-step ability resolves without a choice when no lands are controlled")
    void resolvesWithoutLands() {
        harness.addToBattlefield(player1, new ZellDincht());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Losing Zell removes the additional land permission even after a land was played")
    void losesAdditionalLandPermissionWhenZellLeaves() {
        Permanent zell = harness.addToBattlefieldAndReturn(player1, new ZellDincht());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.playLand(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, zell));

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
    }
}
