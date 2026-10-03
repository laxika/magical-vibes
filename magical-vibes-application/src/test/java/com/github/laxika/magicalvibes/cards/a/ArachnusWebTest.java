package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CudgelTroll;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArachnusWeb.class, RuneclawBear.class, LlanowarElves.class, CudgelTroll.class,
        TitanicGrowth.class, TurnToFrog.class})
class ArachnusWebTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot be declared as an attacker")
    void enchantedCreatureCannotAttack() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        Permanent web = new Permanent(new ArachnusWeb());
        web.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(web);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature's activated abilities cannot be activated")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        Permanent web = new Permanent(new ArachnusWeb());
        web.setAttachedTo(elves.getId());
        gd.playerBattlefields.get(player2.getId()).add(web);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroyed at the beginning of the end step when enchanted creature's power is 4 or greater")
    void destroyedAtEndStepWhenPowerIsFourOrGreater() {
        Permanent elemental = addCreatureReady(player2, new CudgelTroll());

        Permanent web = new Permanent(new ArachnusWeb());
        web.setAttachedTo(elemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(web);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arachnus Web");
        harness.assertInGraveyard(player1, "Arachnus Web");
        // "Destroy this Aura" means the Aura, never the permanent it is attached to.
        harness.assertOnBattlefield(player2, "Cudgel Troll");
    }

    @Test
    @DisplayName("Survives the end step when enchanted creature's power is less than 4")
    void survivesEndStepWhenPowerIsBelowFour() {
        Permanent bears = addCreatureReady(player2, new RuneclawBear());

        Permanent web = new Permanent(new ArachnusWeb());
        web.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(web);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Arachnus Web");
    }

    @Test
    void resolvesAttachedToTargetCreature() {
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new ArachnusWeb()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(web -> {
                    assertThat(web.getCard()).isInstanceOf(ArachnusWeb.class);
                    assertThat(web.getAttachedTo()).isEqualTo(bear.getId());
                });
    }

    @Test
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        attachWeb(player1, blocker);
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void enchantedCreatureCannotActivateNonManaAbility() {
        Permanent troll = addCreatureReady(player1, new CudgelTroll());
        attachWeb(player2, troll);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void survivesWhenPowerDropsBeforeTriggerResolves() {
        Permanent troll = addCreatureReady(player2, new CudgelTroll());
        attachWeb(player1, troll);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player1, 0, troll.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arachnus Web");
        harness.assertOnBattlefield(player2, "Cudgel Troll");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersDuringOpponentsEndStepUsingModifiedPower() {
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        attachWeb(player1, bear);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new TitanicGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Arachnus Web");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void increasingPowerAfterEndStepBeginsDoesNotCreateTrigger() {
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        attachWeb(player1, bear);
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Arachnus Web");
    }

    private void attachWeb(Player player, Permanent creature) {
        Permanent web = harness.addToBattlefieldAndReturn(player, new ArachnusWeb());
        web.setAttachedTo(creature.getId());
    }
}
