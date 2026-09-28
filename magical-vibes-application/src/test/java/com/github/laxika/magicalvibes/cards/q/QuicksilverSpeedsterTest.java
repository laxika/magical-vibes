package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuicksilverSpeedster.class, GrizzlyBears.class})
class QuicksilverSpeedsterTest extends BaseCardTest {

    @Test
    @DisplayName("While tapped, its controller may cast spells as though they had flash")
    void grantsFlashWhileTapped() {
        Permanent quicksilver = addCreatureReady(player1, new QuicksilverSpeedster());
        quicksilver.tap();
        prepareOpponentTurnCast();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("While untapped, it does not grant flash")
    void doesNotGrantFlashWhileUntapped() {
        addCreatureReady(player1, new QuicksilverSpeedster());
        prepareOpponentTurnCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void prepareOpponentTurnCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.getGameService().passPriority(harness.getGameData(), player2);
    }
}
