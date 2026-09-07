package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefensiveManeuvers.class, GrizzlyBears.class, HillGiant.class, AvianChangeling.class})
class DefensiveManeuversTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures of the chosen type get +0/+4 on every battlefield")
    void boostsAllCreaturesOfChosenType() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownGiant = addCreatureReady(player1, new HillGiant());

        castDefensiveManeuvers(player1);
        harness.handleListChoice(player1, "BEAR");

        assertThat(ownBear.getPowerModifier()).isZero();
        assertThat(ownBear.getToughnessModifier()).isEqualTo(4);
        assertThat(opposingBear.getPowerModifier()).isZero();
        assertThat(opposingBear.getToughnessModifier()).isEqualTo(4);
        assertThat(ownGiant.getPowerModifier()).isZero();
        assertThat(ownGiant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A Changeling counts as the chosen creature type")
    void changelingCountsAsChosenType() {
        Permanent changeling = addCreatureReady(player2, new AvianChangeling());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        castDefensiveManeuvers(player1);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(changeling.getToughnessModifier()).isEqualTo(4);
        assertThat(giant.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The +0/+4 modifier wears off at end of turn")
    void modifierWearsOffAtEndOfTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        castDefensiveManeuvers(player1);
        harness.handleListChoice(player1, "BEAR");
        assertThat(bear.getToughnessModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    private void castDefensiveManeuvers(Player caster) {
        harness.setHand(caster, List.of(new DefensiveManeuvers()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 3);
        harness.castInstant(caster, 0);
        harness.passBothPriorities();
    }
}
