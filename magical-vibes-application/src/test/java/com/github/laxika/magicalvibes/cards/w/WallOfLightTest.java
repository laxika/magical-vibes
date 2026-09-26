package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DemonicTorment;
import com.github.laxika.magicalvibes.cards.e.EvilEyeOfOrmsByGore;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfLight.class, DemonicTorment.class, EvilEyeOfOrmsByGore.class})
class WallOfLightTest extends BaseCardTest {

    @Test
    @DisplayName("Wall of Light has protection from black")
    void hasProtectionFromBlack() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfLight());

        assertThat(gqs.hasProtectionFrom(gd, wall, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, wall, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Wall of Light cannot be enchanted by a black Aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfLight());
        harness.setHand(player1, List.of(new DemonicTorment()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Wall of Light prevents combat damage from a black creature")
    void preventsCombatDamageFromBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new EvilEyeOfOrmsByGore());
        attacker.setAttacking(true);

        Permanent wall = addCreatureReady(player2, new WallOfLight());
        wall.setBlocking(true);
        wall.addBlockingTarget(0);

        resolveCombat(player1);

        assertThat(wall.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }
}
