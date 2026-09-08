package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErtaisTrickeryTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a kicked spell")
    void countersKickedSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BlinkOfAnEye blink = new BlinkOfAnEye();
        harness.setHand(player1, List.of(blink));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castKickedInstant(player1, 0, target.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ErtaisTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, blink.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Blink of an Eye");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a spell that was not kicked")
    void cannotTargetNonKickedSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BlinkOfAnEye blink = new BlinkOfAnEye();
        harness.setHand(player1, List.of(blink));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ErtaisTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, blink.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("kicked");
    }
}
