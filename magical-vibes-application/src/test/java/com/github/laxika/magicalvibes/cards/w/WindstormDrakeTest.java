package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArrestersAdmonition;
import com.github.laxika.magicalvibes.cards.a.ArrestersZeal;
import com.github.laxika.magicalvibes.cards.f.FaerieDuelist;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindstormDrake.class, FaerieDuelist.class, SauroformHybrid.class,
        ArrestersAdmonition.class, ArrestersZeal.class})
class WindstormDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control with flying get +1/+0")
    void boostsOtherOwnCreaturesWithFlying() {
        harness.addToBattlefield(player1, new WindstormDrake());
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new FaerieDuelist());

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
    }

    @Test
    @DisplayName("Windstorm Drake does not boost itself")
    void doesNotBoostItself() {
        WindstormDrake card = new WindstormDrake();
        card.setPower(10);
        card.setToughness(10);
        Permanent drake = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(10);
    }

    @Test
    @DisplayName("Nonflying creatures and opponents' flying creatures are unaffected")
    void onlyBoostsOtherOwnCreaturesWithFlying() {
        harness.addToBattlefield(player1, new WindstormDrake());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        Permanent opponentSprite = harness.addToBattlefieldAndReturn(player2, new FaerieDuelist());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSprite)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Drakes boost each other and their bonuses stack on other flyers")
    void multipleDrakesStackWithoutBoostingThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WindstormDrake());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WindstormDrake());
        Permanent duelist = harness.addToBattlefieldAndReturn(player1, new FaerieDuelist());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, duelist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duelist)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returning the Drake to hand immediately removes its bonus")
    void bonusEndsWhenDrakeLeavesBattlefield() {
        harness.forceStep(TurnStep.UPKEEP);
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new WindstormDrake());
        Permanent duelist = harness.addToBattlefieldAndReturn(player1, new FaerieDuelist());
        assertThat(gqs.getEffectivePower(gd, duelist)).isEqualTo(2);
        harness.setHand(player1, List.of(new ArrestersAdmonition()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, drake.getId());

        harness.assertInHand(player1, "Windstorm Drake");
        harness.assertNotOnBattlefield(player1, "Windstorm Drake");
        assertThat(gqs.getEffectivePower(gd, duelist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, duelist)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature that gains flying immediately receives the Drake's bonus")
    void boostsCreatureWithGrantedFlying() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new WindstormDrake());
        Permanent hybrid = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        assertThat(gqs.getEffectivePower(gd, hybrid)).isEqualTo(2);
        harness.setHand(player1, List.of(new ArrestersZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, hybrid.getId());

        assertThat(gqs.getEffectivePower(gd, hybrid)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hybrid)).isEqualTo(4);
    }
}
