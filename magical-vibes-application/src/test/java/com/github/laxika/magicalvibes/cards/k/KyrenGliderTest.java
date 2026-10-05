package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KyrenGlider.class, FreshVolunteers.class, CloudSprite.class})
class KyrenGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Kyren Glider cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new KyrenGlider());

        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Kyren Glider's flying prevents a ground creature from blocking it")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new KyrenGlider());
        addCreatureReady(player2, new FreshVolunteers());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Kyren Glider can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new KyrenGlider());
        Permanent blocker = addCreatureReady(player2, new CloudSprite());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Kyren Glider cannot block even an attacker with flying")
    void cannotBlockFlyingAttacker() {
        Permanent glider = addCreatureReady(player2, new KyrenGlider());
        addCreatureReady(player1, new CloudSprite());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(glider.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Kyren Glider can attack and deal unblocked combat damage")
    void dealsUnblockedCombatDamage() {
        addCreatureReady(player1, new KyrenGlider());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat(player1);

        harness.assertLife(player2, 19);
    }
}
