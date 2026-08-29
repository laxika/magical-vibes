package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MinamoScrollkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's maximum hand size is increased by one")
    void controllerMaximumHandSizeIsIncreasedByOne() {
        harness.addToBattlefield(player1, new MinamoScrollkeeper());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, handOfEightCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Minamo Scrollkeeper does not increase an opponent's maximum hand size")
    void opponentMaximumHandSizeIsUnaffected() {
        harness.addToBattlefield(player1, new MinamoScrollkeeper());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, handOfEightCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Controller's maximum hand size returns to seven when Minamo Scrollkeeper leaves")
    void increaseEndsWhenSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new MinamoScrollkeeper());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, handOfEightCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    private List<Card> handOfEightCards() {
        return new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(), new Plains(), new Plains()
        ));
    }
}
