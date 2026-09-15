package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BattlewiseAven;
import com.github.laxika.magicalvibes.cards.c.CabalTrainee;
import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcaneTeachings.class, BattlewiseAven.class, CabalTrainee.class, NantukoMonastery.class})
class ArcaneTeachingsTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Arcane Teachings puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Arcane Teachings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    // ===== +2/+2 boost =====

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    // ===== Granted activated ability: deal 1 damage to creature =====

    @Test
    @DisplayName("Enchanted creature can tap to deal 1 damage to target creature")
    void grantedAbilityDeals1DamageToCreature() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        Permanent targetCreature = addCreatureReady(player2, new BattlewiseAven());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        // Battlewise Aven has 2 toughness, so 1 damage shouldn't destroy it
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(targetCreature);
        // The enchanted creature should be tapped
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating granted ability puts it on the stack")
    void grantedAbilityPutsOnStack() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        Permanent targetCreature = addCreatureReady(player2, new BattlewiseAven());

        harness.activateAbility(player1, 0, null, targetCreature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Granted ability deals exactly 1 damage, destroying a 1-toughness creature")
    void grantedAbilityDestroysOneToughnessCreature() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        // Cabal Trainee is a 1/1
        Permanent trainee = addCreatureReady(player2, new CabalTrainee());

        harness.activateAbility(player1, 0, null, trainee.getId());
        harness.passBothPriorities();

        // 1 damage kills a 1/1
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(trainee.getId()));
    }

    // ===== Granted activated ability: deal 1 damage to player =====

    @Test
    @DisplayName("Enchanted creature can tap to deal 1 damage to a player")
    void grantedAbilityDeals1DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Already tapped =====

    @Test
    @DisplayName("Already tapped creature cannot use granted tap ability")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());
        creature.tap();

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Effects stop when aura is removed =====

    @Test
    @DisplayName("Creature loses boost and granted ability when Arcane Teachings is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        // Remove Arcane Teachings
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        // Creature should no longer have an activated ability
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Arcane Teachings does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        Permanent otherCreature = addCreatureReady(player1, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        // Other creature should not get the boost
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Arcane Teachings")
    void canTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BattlewiseAven());
        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Arcane Teachings")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new NantukoMonastery());
        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Can enchant opponent's creature =====

    @Test
    @DisplayName("Can enchant opponent's creature and grant it the ability")
    void canEnchantOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        // Opponent's creature should get the boost
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }
}

