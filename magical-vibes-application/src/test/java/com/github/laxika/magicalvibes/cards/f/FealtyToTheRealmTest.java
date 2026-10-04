package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FealtyToTheRealm.class, FountainOfYouth.class, GrizzlyBears.class, JaceBeleren.class, Persuasion.class})
class FealtyToTheRealmTest extends BaseCardTest {

    @Test
    @DisplayName("Makes its controller the monarch and gives the monarch control of the enchanted creature")
    void makesControllerMonarchAndControlsEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFealtyToTheRealm(player1, creature);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.findControllerOf(creature)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Control follows the monarch and the creature cannot attack the Aura's controller")
    void controlFollowsMonarchAndCreatureCannotAttackAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFealtyToTheRealm(player1, creature);

        gd.monarchPlayerId = player2.getId();
        harness.inMutationScope(() -> com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(com.github.laxika.magicalvibes.service.battlefield.CreatureControlService.class)
                .reconcileControl(gd));

        assertThat(gd.findControllerOf(creature)).isEqualTo(player2.getId());
        creature.setSummoningSick(false);
        int creatureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(creature);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(creatureIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The enchanted creature must attack each combat when able")
    void enchantedCreatureMustAttackWhenAble() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFealtyToTheRealm(player1, creature);
        creature.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new FealtyToTheRealm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castFealtyToTheRealm(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new FealtyToTheRealm()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 4);
        harness.castEnchantment(caster, 0, creature.getId());
        resolveAllTriggers();
    }

    @Test
    void canAttackAuraControllersPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        castFealtyToTheRealm(player1, creature);
        gd.monarchPlayerId = player2.getId();
        harness.inMutationScope(() -> com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(com.github.laxika.magicalvibes.service.battlefield.CreatureControlService.class)
                .reconcileControl(gd));
        creature.setSummoningSick(false);
        forceAttackersStep(player2);
        int creatureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(creature);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () ->
                gs.declareAttackers(gd, player2, List.of(creatureIndex),
                        Map.of(creatureIndex, planeswalker.getId())));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    void doesNotOverrideEarlierControlEffectBeforeMonarchTriggerResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Persuasion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();
        assertThat(gd.findControllerOf(creature)).isEqualTo(player1.getId());
        assertThat(gd.monarchPlayerId).isNull();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new FealtyToTheRealm()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isNull();
        assertThat(gd.findControllerOf(creature)).isEqualTo(player1.getId());
    }

    @Test
    void tappedEnchantedCreatureIsNotRequiredToAttack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFealtyToTheRealm(player1, creature);
        creature.setSummoningSick(false);
        creature.setTapped(true);

        declareAttackers(player1, List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    void attackRestrictionAllowsNotAttackingWhenOnlyOpponentIsAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFealtyToTheRealm(player1, creature);
        gd.monarchPlayerId = player2.getId();
        harness.inMutationScope(() -> com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(com.github.laxika.magicalvibes.service.battlefield.CreatureControlService.class)
                .reconcileControl(gd));
        creature.setSummoningSick(false);

        declareAttackers(player2, List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    private void forceAttackersStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
