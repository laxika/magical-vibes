package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShoalSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall removes defender until end of turn")
    void landfallRemovesDefenderUntilEndOfTurn() {
        Permanent serpent = addSerpent(player1);
        harness.setHand(player1, List.of(new Forest()));

        assertThat(gqs.hasKeyword(gd, serpent, Keyword.DEFENDER)).isTrue();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, serpent, Keyword.DEFENDER)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, serpent, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("An opponent's landfall does not remove defender")
    void opponentsLandDoesNotRemoveDefender() {
        Permanent serpent = addSerpent(player1);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, serpent, Keyword.DEFENDER)).isTrue();
    }

    private Permanent addSerpent(Player player) {
        Permanent serpent = new Permanent(new ShoalSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(serpent);
        return serpent;
    }
}
