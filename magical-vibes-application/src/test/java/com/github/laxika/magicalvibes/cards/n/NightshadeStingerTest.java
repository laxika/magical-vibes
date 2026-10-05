package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightshadeStinger.class, WoodlandChangeling.class})
class NightshadeStingerTest extends BaseCardTest {

    @Test
    @DisplayName("Nightshade Stinger cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new NightshadeStinger());

        Permanent attacker = addCreatureReady(player1, new WoodlandChangeling());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Nightshade Stinger cannot block even a flying attacker")
    void cannotBlockFlyingAttacker() {
        addCreatureReady(player2, new NightshadeStinger());
        Permanent attacker = addCreatureReady(player1, new NightshadeStinger());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Nightshade Stinger cannot be blocked by a creature without flying or reach")
    void flyingPreventsGroundBlocker() {
        addCreatureReady(player2, new WoodlandChangeling());
        Permanent attacker = addCreatureReady(player1, new NightshadeStinger());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Nightshade Stinger can attack and deal combat damage")
    void canAttackAndDealCombatDamage() {
        addCreatureReady(player1, new NightshadeStinger());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
