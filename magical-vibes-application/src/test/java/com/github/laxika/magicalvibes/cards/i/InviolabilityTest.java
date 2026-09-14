package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.Thunderclap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Inviolability.class, FreshVolunteers.class, JhovallQueen.class, Mountain.class, Thunderclap.class})
class InviolabilityTest extends BaseCardTest {

    @Test
    @DisplayName("Inviolability prevents noncombat damage to the enchanted creature")
    void preventsNoncombatDamage() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        castInviolability(creature);

        harness.setHand(player2, List.of(new Thunderclap()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Inviolability prevents combat damage to the enchanted creature")
    void preventsCombatDamage() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        castInviolability(creature);
        creature.setBlocking(true);
        creature.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new JhovallQueen());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Inviolability can only target a creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Inviolability()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castInviolability(Permanent creature) {
        harness.setHand(player1, List.of(new Inviolability()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
