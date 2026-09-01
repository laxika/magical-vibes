package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionReinforcements;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiteLotusReinforcements.class, InvasionReinforcements.class, GrizzlyBears.class})
class WhiteLotusReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("Other Allies you control get +1/+1")
    void buffsOtherAlliesYouControl() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new InvasionReinforcements());
        int basePower = gqs.getEffectivePower(gd, ally);
        int baseToughness = gqs.getEffectiveToughness(gd, ally);

        harness.addToBattlefield(player1, new WhiteLotusReinforcements());

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("White Lotus Reinforcements does not buff itself")
    void doesNotBuffItself() {
        WhiteLotusReinforcements card = new WhiteLotusReinforcements();
        card.setPower(10);
        card.setToughness(11);
        Permanent source = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(11);
    }

    @Test
    @DisplayName("Does not buff non-Ally creatures")
    void doesNotBuffNonAllies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.addToBattlefield(player1, new WhiteLotusReinforcements());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not buff an opponent's Allies")
    void doesNotBuffOpponentsAllies() {
        Permanent opponentAlly = harness.addToBattlefieldAndReturn(player2, new InvasionReinforcements());
        int basePower = gqs.getEffectivePower(gd, opponentAlly);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentAlly);

        harness.addToBattlefield(player1, new WhiteLotusReinforcements());

        assertThat(gqs.getEffectivePower(gd, opponentAlly)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, opponentAlly)).isEqualTo(baseToughness);
    }
}
