package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoggRaider.class, FightingDrake.class})
class MoggRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another Goblin gives target creature +1/+1")
    void boostsTargetCreature() {
        setupRaider();
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new MoggRaider());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FightingDrake());

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Mogg Raider");
    }

    @Test
    @DisplayName("Can sacrifice itself to pay the cost")
    void sacrificesItself() {
        setupRaider();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FightingDrake());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mogg Raider");
        assertThat(creature.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupRaider();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FightingDrake());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @CardUsed(BoggartShenanigans.class)
    @DisplayName("Can sacrifice a noncreature Goblin permanent to pay the cost")
    void sacrificesNoncreatureGoblinPermanent() {
        setupRaider();
        Permanent goblinEnchantment = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        harness.addToBattlefield(player1, new MoggRaider());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FightingDrake());

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, goblinEnchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Boggart Shenanigans");
        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        setupRaider();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupRaider() {
        harness.addToBattlefield(player1, new MoggRaider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
