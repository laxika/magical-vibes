package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwordOfTheChosen.class, SliverQueen.class, YouthfulKnight.class})
class SwordOfTheChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives a legendary creature +2/+2 until end of turn")
    void boostsLegendaryCreature() {
        Permanent sword = addCreatureReady(player1, new SwordOfTheChosen());
        Permanent queen = addCreatureReady(player2, new SliverQueen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, queen.getId());
        harness.passBothPriorities();

        assertThat(sword.isTapped()).isTrue();
        assertThat(queen.getEffectivePower()).isEqualTo(9);
        assertThat(queen.getEffectiveToughness()).isEqualTo(9);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(queen.getEffectivePower()).isEqualTo(7);
        assertThat(queen.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Ability cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        addCreatureReady(player1, new SwordOfTheChosen());
        Permanent knight = addCreatureReady(player2, new YouthfulKnight());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary creature");
    }

    @Test
    @DisplayName("Ability cannot target a legendary noncreature permanent")
    void cannotTargetLegendaryNoncreature() {
        addCreatureReady(player1, new SwordOfTheChosen());
        Permanent sword = addCreatureReady(player2, new SwordOfTheChosen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sword.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary creature");
    }

    @Test
    @DisplayName("Ability cannot be activated while Sword of the Chosen is tapped")
    void cannotActivateWhenTapped() {
        addCreatureReady(player1, new SwordOfTheChosen());
        Permanent queen = addCreatureReady(player2, new SliverQueen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, queen.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, queen.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
