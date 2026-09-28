package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToddlersRage.class, GrizzlyBears.class, FountainOfYouth.class})
class ToddlersRageTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+1 and tantrum")
    void boostsEnchantedCreatureAndGrantsTantrum() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castToddlersRage(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TANTRUM)).isTrue();
    }

    @Test
    @DisplayName("The Aura's effect ends when it leaves the battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castToddlersRage(bears);
        Permanent aura = findPermanent(player1, "Toddler's Rage");

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TANTRUM)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ToddlersRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castToddlersRage(Permanent target) {
        harness.setHand(player1, List.of(new ToddlersRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
