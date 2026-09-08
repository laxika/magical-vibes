package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EidolonOfCountlessBattlesTest extends BaseCardTest {

    @Test
    @DisplayName("Eidolon gets +1/+1 for each creature and Aura its controller controls")
    void boostsItselfFromControlledCreaturesAndAuras() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EidolonOfCountlessBattles()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Eidolon of Countless Battles"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(2);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature using its controller's creatures and Auras")
    void boostsEnchantedCreatureFromControlledCreaturesAndAuras() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EidolonOfCountlessBattles()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castWithAlternateCost(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
    }

    @Test
    @DisplayName("A bestowed Eidolon becomes a creature and counts itself when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EidolonOfCountlessBattles()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castWithAlternateCost(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bears)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.runStateBasedActions();

        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(1);
        assertThat(eidolon.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow can target only a creature")
    void bestowRejectsNonCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setHand(player1, List.of(new EidolonOfCountlessBattles()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
