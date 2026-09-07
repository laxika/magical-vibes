package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExoskeletalArmor.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
class ExoskeletalArmorTest extends BaseCardTest {

    @Test
    void boostsEnchantedCreatureByCreatureCardsInAllGraveyards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new ExoskeletalArmor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    void stopsBoostingWhenAuraLeavesTheBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent armor = new Permanent(new ExoskeletalArmor());
        armor.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(armor);
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        gd.playerBattlefields.get(player1.getId()).remove(armor);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ExoskeletalArmor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
