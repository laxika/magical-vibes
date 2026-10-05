package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BoonReflection;
import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerOfFire.class, BriarberryCohort.class, BoonReflection.class, ChandraNalaar.class})
class PowerOfFireTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Power of Fire attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());

        harness.setHand(player1, List.of(new PowerOfFire()));
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached() && p.getAttachedTo().equals(creature.getId()));
    }

    // ===== Granted activated ability =====

    @Test
    @DisplayName("Enchanted creature can tap to deal 1 damage to a player")
    void grantedAbilityDeals1DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new BriarberryCohort());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        auraPerm.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted opponent's creature can use the granted ability")
    void enchantedOpponentsCreatureCanUseGrantedAbility() {
        harness.setLife(player1, 20);

        Permanent creature = addCreatureReady(player2, new BriarberryCohort());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        auraPerm.setAttachedTo(creature.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted ability deals 1 damage to a planeswalker")
    void grantedAbilityDeals1DamageToPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        auraPerm.setAttachedTo(creature.getId());

        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted ability deals exactly 1 damage, destroying a 1-toughness creature")
    void grantedAbilityDestroysOneToughnessCreature() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        auraPerm.setAttachedTo(creature.getId());

        Permanent target = addCreatureReady(player2, new BriarberryCohort());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Activating granted ability puts it on the stack")
    void grantedAbilityPutsOnStack() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        auraPerm.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    // ===== Summoning sickness / tapped =====

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        auraPerm.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Already tapped creature cannot use granted tap ability")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());
        creature.tap();

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        auraPerm.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Effect stops when aura is removed =====

    @Test
    @DisplayName("Creature loses granted ability when Power of Fire is removed")
    void abilityStopsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        auraPerm.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Power of Fire")
    void cannotTargetNonCreature() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new BoonReflection());
        harness.setHand(player1, List.of(new PowerOfFire()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Granted ability can target the enchanted creature itself")
    void grantedAbilityCanTargetItself() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Briarberry Cohort");
        harness.assertInGraveyard(player1, "Briarberry Cohort");
        harness.assertInGraveyard(player1, "Power of Fire");
    }

    @Test
    @DisplayName("Activated ability still resolves after Power of Fire leaves the battlefield")
    void activatedAbilitySurvivesAuraRemoval() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Activated ability still resolves after the enchanted creature leaves the battlefield")
    void activatedAbilitySurvivesCreatureRemoval() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Power of Fire does not resolve when its creature target leaves the battlefield")
    void auraDoesNotResolveWithoutItsTarget() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());
        harness.setHand(player1, List.of(new PowerOfFire()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Power of Fire");
        harness.assertInGraveyard(player1, "Power of Fire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Granted damage ability cannot target a noncreature enchantment")
    void grantedAbilityCannotTargetNoncreatureEnchantment() {
        Permanent creature = addCreatureReady(player1, new BriarberryCohort());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PowerOfFire());
        aura.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BoonReflection());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
