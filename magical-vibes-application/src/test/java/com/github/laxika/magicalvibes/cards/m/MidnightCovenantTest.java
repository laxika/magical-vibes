package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MidnightCovenant.class, WanderingOnes.class, Island.class})
class MidnightCovenantTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Midnight Covenant attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player1, new WanderingOnes());

        harness.setHand(player1, List.of(new MidnightCovenant()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Midnight Covenant")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature can pay {B} to get +1/+1 until end of turn")
    void grantedAbilityBoostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new WanderingOnes());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MidnightCovenant());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the enchanted creature gets the granted ability")
    void onlyEnchantedCreatureGetsGrantedAbility() {
        Permanent enchanted = addCreatureReady(player1, new WanderingOnes());
        Permanent otherCreature = addCreatureReady(player1, new WanderingOnes());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MidnightCovenant());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(otherCreature), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("The granted boost is cumulative but wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new WanderingOnes());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MidnightCovenant());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature loses the granted ability when the aura leaves the battlefield")
    void abilityLostWhenAuraRemoved() {
        Permanent creature = addCreatureReady(player1, new WanderingOnes());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MidnightCovenant());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Midnight Covenant cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new MidnightCovenant()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
