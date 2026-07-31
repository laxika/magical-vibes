package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent elemental = new Permanent(new AirElemental());
        elemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(elemental);

        Permanent aura = new Permanent(new Domestication());
        aura.setAttachedTo(elemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

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
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new Domestication());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Domestication");
    }

    @Test
    @DisplayName("Does not trigger on the opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent elemental = new Permanent(new AirElemental());
        elemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(elemental);

        Permanent aura = new Permanent(new Domestication());
        aura.setAttachedTo(elemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // The end step passes without any trigger going on the stack, so the turn rolls on.
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.assertOnBattlefield(player1, "Domestication");
    }
}
