package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShoalSerpent.class, Forest.class})
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

    @Test
    @DisplayName("Landfall removes defender only when its trigger resolves")
    void defenderRemainsUntilTriggerResolves() {
        Permanent serpent = addSerpent(player1);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gqs.hasKeyword(gd, serpent, Keyword.DEFENDER)).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, serpent, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("A land entering without being played triggers each existing serpent")
    void landEnteringTriggersEachSerpent() {
        Permanent first = addSerpent(player1);
        Permanent second = addSerpent(player1);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("A serpent entering after landfall keeps defender")
    void laterSerpentKeepsDefender() {
        Permanent original = addSerpent(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent later = addSerpent(player1);

        assertThat(gqs.hasKeyword(gd, original, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, later, Keyword.DEFENDER)).isTrue();
    }

    private Permanent addSerpent(Player player) {
        Permanent serpent = harness.addToBattlefieldAndReturn(player, new ShoalSerpent());
        serpent.setSummoningSick(false);
        return serpent;
    }
}
