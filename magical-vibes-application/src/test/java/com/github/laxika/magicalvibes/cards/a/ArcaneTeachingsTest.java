package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcaneTeachings.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class})
class ArcaneTeachingsTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Arcane Teachings puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Arcane Teachings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ArcaneTeachings
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== +2/+2 boost =====

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    // ===== Granted activated ability: deal 1 damage to creature =====

    @Test
    @DisplayName("Enchanted creature can tap to deal 1 damage to target creature")
    void grantedAbilityDeals1DamageToCreature() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        // Bears has 2 toughness, so 1 damage shouldn't destroy it
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(targetCreature);
        // The enchanted creature should be tapped
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating granted ability puts it on the stack")
    void grantedAbilityPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, targetCreature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Granted ability deals exactly 1 damage, destroying a 1-toughness creature")
    void grantedAbilityDestroysOneToughnessCreature() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        Permanent elfPerm = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, elfPerm.getId());
        harness.passBothPriorities();

        // 1 damage kills a 1/1
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(elfPerm);
    }

    // ===== Granted activated ability: deal 1 damage to player =====

    @Test
    @DisplayName("Enchanted creature can tap to deal 1 damage to a player")
    void grantedAbilityDeals1DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Already tapped =====

    @Test
    @DisplayName("Already tapped creature cannot use granted tap ability")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        bearsPerm.tap();

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Effects stop when aura is removed =====

    @Test
    @DisplayName("Creature loses boost and granted ability when Arcane Teachings is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Verify effects are active
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);

        // Remove Arcane Teachings
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        // Verify effects are gone
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);

        // Creature should no longer have an activated ability
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Arcane Teachings does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Other creature should not get the boost
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Arcane Teachings")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Arcane Teachings")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ArcaneTeachings()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Can enchant opponent's creature =====

    @Test
    @DisplayName("Can enchant opponent's creature and grant it the ability")
    void canEnchantOpponentCreature() {
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        // Opponent's creature should get the boost
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted opponent's creature can use the granted ability")
    void opponentControlsEnchantedCreatureCanUseGrantedAbility() {
        harness.setLife(player1, 20);
        Permanent bearsPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneTeachings());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(bearsPerm.isTapped()).isTrue();
    }
}

