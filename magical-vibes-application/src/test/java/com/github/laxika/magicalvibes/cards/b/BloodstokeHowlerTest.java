package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EmberBeast;
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

@CardUsed({BloodstokeHowler.class, EmberBeast.class, GrizzlyBears.class})
class BloodstokeHowlerTest extends BaseCardTest {

    @Test
    @DisplayName("Turning Bloodstoke Howler face up boosts your Beasts until end of turn")
    void turningFaceUpBoostsOwnBeastsOnly() {
        Permanent otherBeast = harness.addToBattlefieldAndReturn(player1, new EmberBeast());
        Permanent nonBeast = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBeast = harness.addToBattlefieldAndReturn(player2, new EmberBeast());
        Permanent howler = castFaceDown();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(howler));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, otherBeast)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, nonBeast)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBeast)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherBeast)).isEqualTo(3);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new BloodstokeHowler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Bloodstoke Howler");
    }
}
