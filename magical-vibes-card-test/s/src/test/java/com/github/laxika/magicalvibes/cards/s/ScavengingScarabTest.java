package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ScavengingScarab.class)
class ScavengingScarabTest extends BaseCardTest {

    @Test
    @DisplayName("Scavenging Scarab can attack and deal combat damage")
    void canAttackAndDealCombatDamage() {
        addCreatureReady(player1, new ScavengingScarab());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Scavenging Scarab cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new ScavengingScarab());
        addCreatureReady(player1, new ScavengingScarab());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
