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

@CardUsed(FracturedPowerstone.class)
class FracturedPowerstoneTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new FracturedPowerstone());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
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

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.planechase.rollSequence).isEqualTo(1);
        assertThat(gd.planechase.lastRollPlayerId).isEqualTo(player1.getId());
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
}
