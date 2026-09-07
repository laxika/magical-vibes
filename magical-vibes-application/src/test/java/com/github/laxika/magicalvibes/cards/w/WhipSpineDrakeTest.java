package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WhipSpineDrake.class)
class WhipSpineDrakeTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new WhipSpineDrake()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent drake = findPermanent(player1, "Whip-Spine Drake");
        assertThat(drake.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int drakeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drake);
        harness.turnFaceUp(player1, drakeIndex);
        harness.passBothPriorities();

        assertThat(drake.isFaceDown()).isFalse();
    }
}
