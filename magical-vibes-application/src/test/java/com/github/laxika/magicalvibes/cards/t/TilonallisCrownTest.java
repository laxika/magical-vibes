package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TilonallisCrownTest extends BaseCardTest {

    @Test
    void etbDamagesAndBoostsEnchantedCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new TilonallisCrown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void effectsStopWhenAuraIsRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent aura = new Permanent(new TilonallisCrown());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotEnchantNoncreaturePermanent() {
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        harness.setHand(player1, List.of(new TilonallisCrown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
