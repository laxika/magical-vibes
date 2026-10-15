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

    @Test
    @DisplayName("Granted ability still resolves after the Aura leaves the battlefield")
    void grantedAbilitySurvivesAuraRemoval() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.damageDealtThisTurnBySource.get(creature.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Granted ability still resolves after its creature is sacrificed")
    void grantedAbilitySurvivesCreatureSacrifice() {
        Permanent creature = addCreatureReady(player1, new CabalTrainee());
        Permanent otherCreature = addCreatureReady(player1, new BattlewiseAven());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.activateAbility(player1, 0, 0, null, otherCreature.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Cabal Trainee");
        harness.assertInGraveyard(player1, "Arcane Teachings");
    }

    @Test
    @DisplayName("Aura does not resolve if its targeted creature leaves the battlefield")
    void auraDoesNotResolveAfterTargetIsSacrificed() {
        Permanent creature = addCreatureReady(player1, new CabalTrainee());
        Permanent otherCreature = addCreatureReady(player1, new BattlewiseAven());
        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.activateAbility(player1, 0, null, otherCreature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Arcane Teachings");
        harness.assertInGraveyard(player1, "Arcane Teachings");
    }

    @Test
    @DisplayName("Granted damage ability cannot target a noncreature land")
    void grantedAbilityCannotTargetNoncreatureLand() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        aura.setAttachedTo(creature.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new NantukoMonastery());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enchanted creature can target itself and receives exactly one damage")
    void grantedAbilityCanDamageItsOwnSource() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting Arcane Teachings puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Arcane Teachings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

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

    @Test
    @DisplayName("Enchanted opponent's creature can use the granted ability")
    void opponentControlsEnchantedCreatureCanUseGrantedAbility() {
        harness.setLife(player1, 20);
        Permanent bearsPerm = addCreatureReady(player2, new BattlewiseAven());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(bearsPerm.isTapped()).isTrue();
    }
}
