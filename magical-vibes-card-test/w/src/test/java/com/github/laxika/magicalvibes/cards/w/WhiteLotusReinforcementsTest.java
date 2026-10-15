package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CatGator;
import com.github.laxika.magicalvibes.cards.i.InvasionReinforcements;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiteLotusReinforcements.class, InvasionReinforcements.class, CatGator.class})
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
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new CatGator());
        int basePower = gqs.getEffectivePower(gd, nonAlly);
        int baseToughness = gqs.getEffectiveToughness(gd, nonAlly);

        harness.addToBattlefield(player1, new WhiteLotusReinforcements());

        assertThat(gqs.getEffectivePower(gd, nonAlly)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, nonAlly)).isEqualTo(baseToughness);
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

    @Test
    @DisplayName("Vigilance allows attacking without tapping")
    void attacksWithoutTapping() {
        Permanent source = addCreatureReady(player1, new WhiteLotusReinforcements());

        declareAttackers(List.of(0));

        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple copies boost each other and their bonuses stack on other Allies")
    void multipleCopiesStack() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new InvasionReinforcements());
        int allyPower = gqs.getEffectivePower(gd, ally);
        int allyToughness = gqs.getEffectiveToughness(gd, ally);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WhiteLotusReinforcements());
        int sourcePower = gqs.getEffectivePower(gd, first);
        int sourceToughness = gqs.getEffectiveToughness(gd, first);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WhiteLotusReinforcements());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(sourcePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(sourceToughness + 1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(sourcePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(sourceToughness + 1);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(allyPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(allyToughness + 2);
    }

    @Test
    @DisplayName("The bonus ends when White Lotus Reinforcements dies")
    void bonusEndsWhenSourceDies() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new InvasionReinforcements());
        int basePower = gqs.getEffectivePower(gd, ally);
        int baseToughness = gqs.getEffectiveToughness(gd, ally);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new WhiteLotusReinforcements());
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(baseToughness + 1);

        source.setMarkedDamage(gqs.getEffectiveToughness(gd, source));
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "White Lotus Reinforcements");
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(baseToughness);
    }
}
