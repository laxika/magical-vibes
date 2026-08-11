package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AncestralBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Ancestral Blade creates and equips a 1/1 Soldier token")
    void enteringCreatesAndEquipsSoldier() {
        harness.setHand(player1, List.of(new AncestralBlade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Ancestral Blade");
        Permanent soldier = findPermanent(player1, "Soldier");

        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(blade.getAttachedTo()).isEqualTo(soldier.getId());
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {1} moves Ancestral Blade and its bonus to another creature")
    void equipMovesBladeAndBonus() {
        harness.setHand(player1, List.of(new AncestralBlade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Ancestral Blade");
        Permanent soldier = findPermanent(player1, "Soldier");

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
    }
}
