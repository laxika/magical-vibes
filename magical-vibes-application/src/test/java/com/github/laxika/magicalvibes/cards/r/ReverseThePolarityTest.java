package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReverseThePolarity.class, GiantSpider.class, GrizzlyBears.class, LightningBolt.class})
class ReverseThePolarityTest extends BaseCardTest {

    @Test
    void countersAllOtherSpells() {
        LightningBolt firstBolt = new LightningBolt();
        LightningBolt secondBolt = new LightningBolt();
        harness.setHand(player1, List.of(firstBolt, secondBolt));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new ReverseThePolarity()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertInGraveyard(player2, "Reverse the Polarity");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void switchesEachCreaturePowerAndToughnessUntilEndOfTurn() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new ReverseThePolarity()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
    }

    @Test
    void makesAllCreaturesUnblockableUntilEndOfTurn() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReverseThePolarity()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, ownBear)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, opposingBear)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, ownBear)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, opposingBear)).isFalse();
    }
}
