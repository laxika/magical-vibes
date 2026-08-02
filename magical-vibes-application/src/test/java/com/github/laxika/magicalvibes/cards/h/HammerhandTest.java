package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HammerhandTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and has haste")
    void enchantedCreatureBoostedAndHaste() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new Hammerhand());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses the boost and haste when Hammerhand leaves")
    void effectsStopWhenRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new Hammerhand());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("ETB can't-block hits the targeted creature, not the enchanted creature")
    void etbCantBlockHitsTargetNotEnchanted() {
        Permanent mine = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(mine);
        Permanent opponentBlocker = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBlocker);

        harness.setHand(player1, List.of(new Hammerhand()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Group 0 = enchant target, group 1 = target creature that can't block.
        harness.castEnchantment(player1, 0, List.of(mine.getId(), opponentBlocker.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentBlocker.isCantBlockThisTurn()).isTrue();
        assertThat(mine.isCantBlockThisTurn()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hammerhand")
                        && mine.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Hammerhand can enchant a creature an opponent controls")
    void canEnchantOpponentCreature() {
        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);
        Permanent mine = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(mine);

        harness.setHand(player1, List.of(new Hammerhand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, List.of(opponentBears.getId(), mine.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isTrue();
        assertThat(mine.isCantBlockThisTurn()).isTrue();
    }
}
