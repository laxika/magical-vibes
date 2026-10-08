package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImplementsOfSacrifice;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToweringViewpoint.class, GrizzlyBears.class, ImplementsOfSacrifice.class})
class ToweringViewpointTest extends BaseCardTest {

    @Test
    void grantsFlyingToTargetCreature() {
        harness.addToBattlefieldAndReturn(player1, new ToweringViewpoint());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void canTargetAnOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new ToweringViewpoint());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new ToweringViewpoint());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new ToweringViewpoint());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ImplementsOfSacrifice());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent viewpoint = harness.addToBattlefieldAndReturn(player1, new ToweringViewpoint());
        viewpoint.tap();
        viewpoint.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, viewpoint.getId());
        assertThat(viewpoint.hasKeyword(Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(viewpoint.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(viewpoint.isTapped()).isTrue();
    }

    @Test
    void canActivateTwiceToGrantFlyingToDifferentCreatures() {
        Permanent viewpoint = harness.addToBattlefieldAndReturn(player1, new ToweringViewpoint());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ToweringViewpoint());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, viewpoint.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, other.getId());
        harness.passBothPriorities();

        assertThat(viewpoint.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(other.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(viewpoint.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithOnlyTwoMana() {
        Permanent viewpoint = harness.addToBattlefieldAndReturn(player1, new ToweringViewpoint());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, viewpoint.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(viewpoint.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
