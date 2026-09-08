package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeliodsPunishment.class, GrizzlyBears.class})
class HeliodsPunishmentTest extends BaseCardTest {

    @Test
    @DisplayName("Heliod's Punishment enters with four task counters")
    void entersWithFourTaskCounters() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeliodsPunishment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent punishment = findPermanent(player1, "Heliod's Punishment");
        assertThat(punishment.getCounterCount(CounterType.TASK)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enchanted creature cannot attack or block")
    void enchantedCreatureCannotAttackOrBlock() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent punishment = harness.addToBattlefieldAndReturn(player2, new HeliodsPunishment());
        punishment.setAttachedTo(enchanted.getId());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("The enchanted creature removes a task counter from the Aura and destroys it at zero")
    void grantedAbilityUsesAuraCountersAndDestroysAuraAtZero() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent punishment = harness.addToBattlefieldAndReturn(player2, new HeliodsPunishment());
        punishment.setAttachedTo(enchanted.getId());
        punishment.setCounterCount(CounterType.TASK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(punishment.getCounterCount(CounterType.TASK)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(punishment);
        harness.assertInGraveyard(player2, "Heliod's Punishment");
    }

    @Test
    @DisplayName("The granted ability cannot be activated without a task counter on the Aura")
    void grantedAbilityRequiresTaskCounterOnAura() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent punishment = harness.addToBattlefieldAndReturn(player2, new HeliodsPunishment());
        punishment.setAttachedTo(enchanted.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters to remove");
    }
}
