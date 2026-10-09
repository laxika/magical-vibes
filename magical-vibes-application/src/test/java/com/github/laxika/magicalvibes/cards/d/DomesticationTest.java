package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WaterServant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Domestication.class, AirElemental.class, GrizzlyBears.class, WaterServant.class})
class DomesticationTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Domestication steals the enchanted creature")
    void stealsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Domestication()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    @Test
    @DisplayName("Sacrificed at the beginning of the controller's end step when power is 4 or greater")
    void sacrificedAtControllerEndStepWhenPowerIsFourOrGreater() {
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Domestication());
        aura.setAttachedTo(elemental.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Domestication");
        harness.assertInGraveyard(player1, "Domestication");
    }

    @Test
    @DisplayName("Survives the end step when enchanted creature's power is less than 4")
    void survivesEndStepWhenPowerIsBelowFour() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Domestication());
        aura.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Domestication");
    }

    @Test
    @DisplayName("Does not trigger on the opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Domestication());
        aura.setAttachedTo(elemental.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // The end step passes without any trigger going on the stack, so the turn rolls on.
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.assertOnBattlefield(player1, "Domestication");
    }

    @Test
    @CardUsed({Domestication.class, WaterServant.class})
    @DisplayName("Temporary power increases trigger sacrifice and return the creature")
    void temporaryPowerIncreaseCausesSacrificeAndReturnsControl() {
        Permanent creature = stealWaterServant();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature), 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Domestication");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @CardUsed({Domestication.class, WaterServant.class})
    @DisplayName("Reducing power below four in response prevents sacrifice")
    void rechecksPowerWhenTriggerResolves() {
        Permanent creature = stealWaterServant();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature), 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature), 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Domestication");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertNotInGraveyard(player1, "Domestication");
    }

    @Test
    @CardUsed({Domestication.class, WaterServant.class})
    @DisplayName("Increasing power after the end step begins does not trigger sacrifice")
    void powerIncreaseAfterEndStepBeginsDoesNotTrigger() {
        Permanent creature = stealWaterServant();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Domestication");
    }

    private Permanent stealWaterServant() {
        Permanent creature = addCreatureReady(player2, new WaterServant());
        harness.setHand(player1, List.of(new Domestication()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        return creature;
    }
}
