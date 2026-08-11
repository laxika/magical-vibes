package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SageEyeHarrierTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new SageEyeHarrier()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent harrier = findPermanent(player1, "Sage-Eye Harrier");
        assertThat(harrier.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int harrierIndex = gd.playerBattlefields.get(player1.getId()).indexOf(harrier);
        harness.turnFaceUp(player1, harrierIndex);
        harness.passBothPriorities();

        assertThat(harrier.isFaceDown()).isFalse();
    }
}
