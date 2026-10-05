package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BranchblightStalker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProstheticInjector.class, GrizzlyBears.class, BranchblightStalker.class})
class ProstheticInjectorTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+2 and toxic")
    void equippedCreatureGetsBoostAndToxic() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent injector = addInjector(player1);
        injector.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TOXIC)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature gives one poison counter on combat damage")
    void equippedCreatureGivesPoisonCounterOnCombatDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent injector = addInjector(player1);
        injector.setAttachedTo(creature.getId());

        creature.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Unattached Injector does not grant toxic or poison")
    void unattachedInjectorHasNoEffect() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addInjector(player1);

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TOXIC)).isFalse();

        creature.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Equip attaches Injector to a creature")
    void equipAttachesToCreature() {
        Permanent injector = addInjector(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(injector.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addInjector(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ProstheticInjector());
    }

    @Test
    @DisplayName("Toxic applies poison with combat damage without using the stack")
    void poisonIsAppliedBeforePlayersReceivePriority() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addInjector(player1).setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            resolveCombat();
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("Granted toxic adds to a creature's existing toxic value")
    void grantedToxicAddsToExistingToxic() {
        Permanent creature = addCreatureReady(player1, new BranchblightStalker());
        addInjector(player1).setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            resolveCombat();
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("Moving the equipment removes its bonuses from the previous creature")
    void reequippingMovesBonuses() {
        Permanent injector = addInjector(player1);
        Permanent previous = addCreatureReady(player1, new GrizzlyBears());
        Permanent next = addCreatureReady(player1, new GrizzlyBears());
        injector.setAttachedTo(previous.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, next.getId());
        harness.passBothPriorities();

        assertThat(injector.getAttachedTo()).isEqualTo(next.getId());
        assertThat(gqs.getEffectiveToughness(gd, previous)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, previous, Keyword.TOXIC)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, next)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, next, Keyword.TOXIC)).isTrue();
    }
}
