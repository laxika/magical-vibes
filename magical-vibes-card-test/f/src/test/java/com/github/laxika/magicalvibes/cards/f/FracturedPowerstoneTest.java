package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FracturedPowerstone.class})
class FracturedPowerstoneTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new FracturedPowerstone());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The second ability rolls the planar die")
    void rollsPlanarDie() {
        PlanechaseService planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        planar.initializeDeck(gd);
        gd.startingPlayerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> planar.start(gd));
        harness.addToBattlefield(player1, new FracturedPowerstone());
        gd.planechase.recordSpecialAction(player1.getId(), gd.turnNumber);
        gd.planechase.recordSpecialAction(player1.getId(), gd.turnNumber);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.planechase.rollSequence).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.planechase.rollSequence).isEqualTo(1);
        assertThat(gd.planechase.lastRollPlayerId).isEqualTo(player1.getId());
        assertThat(gd.planechase.rollCost(player1.getId(), gd.turnNumber)).isEqualTo(2);
    }

    @Test
    @DisplayName("The planar die ability can only be activated at sorcery speed")
    void planarDieAbilityIsSorcerySpeed() {
        harness.addToBattlefield(player1, new FracturedPowerstone());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The planar die ability resolves without effect outside Planechase")
    void planarDieAbilityHasNoEffectOutsidePlanechase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new FracturedPowerstone());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.planechase).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The planar die ability cannot be activated with a spell on the stack")
    void planarDieAbilityRequiresEmptyStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new FracturedPowerstone());
        harness.castFromHand(player1, new FracturedPowerstone(), "{2}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Using the mana ability also pays the shared tap cost")
    void tappedPowerstoneCannotRollPlanarDie() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new FracturedPowerstone());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }
}
