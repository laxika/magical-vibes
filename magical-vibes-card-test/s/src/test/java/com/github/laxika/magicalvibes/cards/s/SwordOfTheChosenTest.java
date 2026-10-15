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
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
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

    @Test
    @DisplayName("A newly entered Sword can boost its controller's legendary creature")
    void canActivateImmediatelyAndTargetOwnCreature() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheChosen());
        Permanent queen = addCreatureReady(player1, new SliverQueen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, queen.getId());
        harness.passBothPriorities();

        assertThat(sword.isTapped()).isTrue();
        assertThat(queen.getEffectivePower()).isEqualTo(9);
        assertThat(queen.getEffectiveToughness()).isEqualTo(9);
    }

    @Test
    @DisplayName("The ability resolves even if Sword leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent sword = addCreatureReady(player1, new SwordOfTheChosen());
        Permanent queen = addCreatureReady(player2, new SliverQueen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, queen.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sword);
        gd.playerGraveyards.get(player1.getId()).add(sword.getCard());
        harness.passBothPriorities();

        assertThat(queen.getEffectivePower()).isEqualTo(9);
        assertThat(queen.getEffectiveToughness()).isEqualTo(9);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not boost a legendary creature that left and returned")
    void doesNotBoostReturnedTarget() {
        Permanent sword = addCreatureReady(player1, new SwordOfTheChosen());
        Permanent queen = addCreatureReady(player2, new SliverQueen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, queen.getId());
        gd.playerBattlefields.get(player2.getId()).remove(queen);
        Permanent returnedQueen = harness.addToBattlefieldAndReturn(player2, queen.getCard());
        harness.passBothPriorities();

        assertThat(sword.isTapped()).isTrue();
        assertThat(returnedQueen.getEffectivePower()).isEqualTo(7);
        assertThat(returnedQueen.getEffectiveToughness()).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }
}
