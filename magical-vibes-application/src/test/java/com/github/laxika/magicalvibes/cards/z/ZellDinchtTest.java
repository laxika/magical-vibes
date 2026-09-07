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
        harness.passBothPriorities();
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
}
