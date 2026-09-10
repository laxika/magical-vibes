package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootwaterMatriarch.class, TrainedArmodon.class, Pacifism.class, Disenchant.class,
        DarkBanishing.class})
class RootwaterMatriarchTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new RootwaterMatriarch(), "{2}{U}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(RootwaterMatriarch.class);
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new RootwaterMatriarch(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof RootwaterMatriarch);
    }

    // ===== Activated ability: stealing an enchanted creature =====

    @Test
    @DisplayName("Activating ability targeting enchanted creature puts it on the stack")
    void activatingTargetingEnchantedCreaturePutsOnStack() {
        addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(RootwaterMatriarch.class);
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Activating ability taps Rootwater Matriarch")
    void activatingTapsMatriarch() {
        Permanent matriarch = addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(matriarch.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability steals enchanted creature")
    void resolvingStealsEnchantedCreature() {
        addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        // Creature should now be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Stolen creature should be summoning sick
        assertThat(creature.isSummoningSick()).isTrue();

        // Creature should be tracked as stolen
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
        assertThat(gd.newestControlEffectFor(creature.getId())).isNotNull();
    }

    @Test
    @DisplayName("Control remains after Rootwater Matriarch untaps")
    void controlRemainsAfterMatriarchUntaps() {
        Permanent matriarch = addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);

        assertThat(matriarch.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Control remains after Rootwater Matriarch leaves the battlefield")
    void controlRemainsAfterMatriarchLeavesBattlefield() {
        Permanent matriarch = addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, matriarch.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(matriarch.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    // ===== Not enchanted: ability does nothing =====

    @Test
    @DisplayName("Ability does nothing if target creature is not enchanted at resolution")
    void abilityDoesNothingIfNotEnchantedAtResolution() {
        addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        Permanent aura = attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());

        // Remove the aura before resolution
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.passBothPriorities();

        // Creature should still be on player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can target an unenchanted creature (but ability does nothing on resolution)")
    void canTargetUnenchantedCreature() {
        addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());

        // Targeting should succeed — "enchanted" is a duration, not a targeting restriction
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        // Creature should stay with player2
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gameLogContains("not enchanted")).isTrue();
    }

    // ===== Creature returns when no longer enchanted =====

    @Test
    @DisplayName("Creature returns to owner when aura is destroyed")
    void creatureReturnsWhenAuraDestroyed() {
        addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        Permanent aura = attachAura(player1, creature, new Pacifism());

        // Steal the creature
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        destroyWithDisenchant(player2, aura);

        // Creature should return to player2
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Tracking should be cleaned up
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
        assertThat(gd.controlEffectsFor(creature.getId())).isEmpty();
    }

    @Test
    @DisplayName("Control does not resume if the creature is enchanted again after losing all Auras")
    void controlDoesNotResumeAfterCreatureIsReenchanted() {
        addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        Permanent aura = attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        destroyWithDisenchant(player2, aura);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Creature stays stolen if a second aura remains after first is destroyed")
    void creatureStaysStolenIfSecondAuraRemains() {
        addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        Permanent firstAura = attachAura(player1, creature, new Pacifism());

        // Steal the creature
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // Attach a second aura after stealing
        attachAura(player2, creature, new Pacifism());

        destroyWithDisenchant(player2, firstAura);

        // Creature should STILL be on player1's battlefield — second Pacifism is still attached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
        assertThat(gd.newestControlEffectFor(creature.getId())).isNotNull();
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Cannot activate ability when summoning sick")
    void cannotActivateWhenSummoningSick() {
        Permanent matriarch = addCreatureReady(player1, new RootwaterMatriarch());
        matriarch.setSummoningSick(true);

        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        attachAura(player1, creature, new Pacifism());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    // ===== Cannot activate when tapped =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent matriarch = addReadyMatriarch(player1);
        matriarch.tap();
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        attachAura(player1, creature, new Pacifism());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Target validation =====

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyMatriarch(player1);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Stealing adds to game log")
    void stealingAddsToGameLog() {
        addReadyMatriarch(player1);
        Permanent creature = addCreatureReady(player2, new TrainedArmodon());
        attachAura(player1, creature, new Pacifism());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("gains control of")).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyMatriarch(Player player) {
        return addCreatureReady(player, new RootwaterMatriarch());
    }

    private Permanent attachAura(Player player, Permanent target, Card auraCard) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player, auraCard);
        auraPerm.setAttachedTo(target.getId());
        return auraPerm;
    }

    private void destroyWithDisenchant(Player caster, Permanent target) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Disenchant()));
        harness.addMana(caster, ManaColor.WHITE, 2);
        harness.passPriority(player1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}

