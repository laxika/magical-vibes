package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClayStatue.class, GrizzlyBears.class, Shatter.class})
class ClayStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack with the statue as its source")
    void activatingAbilityPutsOnStack() {
        Permanent perm = addCreatureReady(player1, new ClayStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Activating regeneration ability consumes two generic mana")
    void activatingAbilityConsumesTwoGenericMana() {
        addCreatureReady(player1, new ClayStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Clay Statue cannot activate regeneration without two generic mana")
    void cannotActivateWithoutTwoGenericMana() {
        addCreatureReady(player1, new ClayStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addCreatureReady(player1, new ClayStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent statue = findPermanent(player1, "Clay Statue");
        assertThat(statue.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Clay Statue from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent statuePerm = addCreatureReady(player1, new ClayStatue());
        statuePerm.setRegenerationShield(1);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Clay Statue");
        Permanent statue = findPermanent(player1, "Clay Statue");
        assertThat(statue.isTapped()).isTrue();
        assertThat(statue.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Clay Statue dies in combat without regeneration shield")
    void diesWithoutRegenerationShield() {
        addCreatureReady(player1, new ClayStatue());

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Clay Statue");
        harness.assertInGraveyard(player1, "Clay Statue");
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent statue = harness.addToBattlefieldAndReturn(player1, new ClayStatue());
        statue.setSummoningSick(true);
        statue.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(statue.getRegenerationShield()).isEqualTo(1);
        assertThat(statue.isTapped()).isTrue();
    }

    @Test
    void creatingShieldDoesNotTapOrRemoveCreatureFromCombat() {
        Permanent statue = addCreatureReady(player1, new ClayStatue());
        statue.setBlocking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(statue.getRegenerationShield()).isEqualTo(1);
        assertThat(statue.isTapped()).isFalse();
        assertThat(statue.isBlocking()).isTrue();
    }

    @Test
    void multipleActivationsProtectAgainstSeparateDestructionEvents() {
        Permanent statue = addCreatureReady(player1, new ClayStatue());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        assertThat(statue.getRegenerationShield()).isEqualTo(2);

        for (int remaining = 1; remaining >= 0; remaining--) {
            harness.setHand(player2, List.of(new Shatter()));
            harness.addMana(player2, ManaColor.RED, 2);
            harness.castAndResolveInstant(player2, 0, statue.getId());

            harness.assertOnBattlefield(player1, "Clay Statue");
            assertThat(statue.getRegenerationShield()).isEqualTo(remaining);
            assertThat(statue.isTapped()).isTrue();
        }

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, statue.getId());

        harness.assertNotOnBattlefield(player1, "Clay Statue");
        harness.assertInGraveyard(player1, "Clay Statue");
    }
}
