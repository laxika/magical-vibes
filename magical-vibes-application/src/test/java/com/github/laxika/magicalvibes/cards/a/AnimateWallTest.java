package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnimateWall.class, GrizzlyBears.class, WallOfStone.class})
class AnimateWallTest extends BaseCardTest {

    private Permanent addWall() {
        return addCreatureReady(player1, new WallOfStone());
    }

    private Permanent attachAnimateWall(Permanent wall) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AnimateWall());
        aura.setAttachedTo(wall.getId());
        return aura;
    }

    // ===== Attacking despite defender =====

    @Test
    @DisplayName("Enchanted Wall can attack as though it didn't have defender")
    void enchantedWallCanAttack() {
        Permanent wall = addWall();
        attachAnimateWall(wall);
        // A blocker on the defending side so combat pauses at declare-blockers (isAttacking stays set).
        harness.addToBattlefield(player2, new GrizzlyBears());
        int wallIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wall);

        declareAttackers(List.of(wallIndex));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Wall cannot attack without Animate Wall (defender)")
    void wallCannotAttackWithoutAura() {
        addWall();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Wall cannot attack after Animate Wall leaves the battlefield")
    void wallCannotAttackAfterAuraRemoved() {
        Permanent wall = addWall();
        Permanent aura = attachAnimateWall(wall);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        int wallIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wall);

        assertThatThrownBy(() -> declareAttackers(List.of(wallIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    // ===== Targeting restriction (Enchant Wall) =====

    @Test
    @DisplayName("Can enchant a Wall")
    void canEnchantWall() {
        Permanent wall = addWall();
        harness.setHand(player1, List.of(new AnimateWall()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, wall.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a non-Wall creature")
    void cannotEnchantNonWall() {
        // A legal Wall target exists (so the Aura is playable), but we aim at the non-Wall.
        addWall();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new AnimateWall()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Wall");
    }
}
