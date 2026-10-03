package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Akoum.class, GrizzlyBears.class, GroundSeal.class, HolyStrength.class})
class AkoumTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void setupPlanarState() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Akoum(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void anyPlayerCanCastEnchantmentAtInstantSpeed() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new GroundSeal()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void chaosDestroysOnlyAnUnenchantedCreature() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(enchantedCreature.getId());
        Permanent unenchantedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        chooseChaosTarget(unenchantedCreature, enchantedCreature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(unenchantedCreature);
    }

    @Test
    void auraCanSaveChaosTargetByEnchantingItBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new GroundSeal());
        chooseChaosTarget(creature, noncreature);
        harness.setHand(player2, List.of(new HolyStrength()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> creature.getId().equals(permanent.getAttachedTo()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void flashPermissionEndsWhenAkoumLeaves() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.planechase.faceUp.clear();
        harness.setHand(player2, List.of(new GroundSeal()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashPermissionDoesNotApplyToNonEnchantmentCreatures() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void chooseChaosTarget(Permanent target, Permanent illegalTarget) {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextSpellTargetTrigger(gd));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, illegalTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
    }
}
