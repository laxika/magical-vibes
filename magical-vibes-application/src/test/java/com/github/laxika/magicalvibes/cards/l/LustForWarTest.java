package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.k.KnightOfCliffhaven;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LustForWar.class, GlorySeeker.class, PropheticPrism.class, KnightOfCliffhaven.class})
class LustForWarTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GlorySeeker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new LustForWar()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving Lust for War attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new LustForWar()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lust for War")
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Tapping the enchanted creature deals 3 damage to its controller")
    void tappingEnchantedCreatureDamagesItsController() {
        Permanent creature = attachAura(player2);
        harness.setLife(player2, 20);

        creature.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, creature));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The tap damage goes to the enchanted creature controller, not the Aura controller")
    void tappingEnchantedCreatureDamagesEnchantedController() {
        Permanent creature = attachAura(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        creature.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, creature));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The enchanted creature must attack each combat if able")
    void enchantedCreatureMustAttackWhenAble() {
        Permanent creature = attachAura(player1);
        creature.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void attackingTriggersDamageBeforeCombatDamage() {
        Permanent creature = attachAura(player2);
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    void summoningSickCreatureIsNotRequiredToAttack() {
        Permanent creature = attachAura(player1);
        creature.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(creature.isAttacking()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    void tappedCreatureIsNotRequiredToAttack() {
        Permanent creature = attachAura(player1);
        creature.setSummoningSick(false);
        creature.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    void vigilanceAttackDoesNotTriggerTapDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KnightOfCliffhaven());
        creature.setCounterCount(CounterType.LEVEL, 4);
        creature.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LustForWar());
        aura.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void damageUsesCreatureControllerAtResolution() {
        Permanent creature = attachAura(player2);
        creature.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, creature));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    private Permanent attachAura(Player creatureController) {
        Permanent creature = harness.addToBattlefieldAndReturn(creatureController, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LustForWar());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
