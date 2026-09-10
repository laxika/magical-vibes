package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderingTroubadour.class, Forest.class})
class WanderingTroubadourTest extends BaseCardTest {

    @Test
    @DisplayName("Ventures into a dungeon at your end step after a land enters under your control")
    void venturesAfterLandEnters() {
        harness.addToBattlefield(player1, new WanderingTroubadour());
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Does not venture at your end step when no land entered under your control")
    void doesNotVentureWithoutLandEntering() {
        harness.addToBattlefield(player1, new WanderingTroubadour());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Does not count a land that entered under an opponent's control")
    void doesNotVentureForOpponentsLand() {
        harness.addToBattlefield(player1, new WanderingTroubadour());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(new Forest()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
