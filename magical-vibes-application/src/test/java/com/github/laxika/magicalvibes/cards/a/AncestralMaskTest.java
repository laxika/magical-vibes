package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FamiliarGround;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AncestralMaskTest extends BaseCardTest {

    @Test
    void doesNotCountItself() {
        Permanent bears = addCreature(player1);
        attachMask(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void getsTwoForEachOtherEnchantmentOnTheBattlefield() {
        Permanent bears = addCreature(player1);
        attachMask(bears);

        harness.addToBattlefield(player1, new FamiliarGround());
        harness.addToBattlefield(player2, new FamiliarGround());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    void boostUpdatesAsOtherEnchantmentsEnterAndLeave() {
        Permanent bears = addCreature(player1);
        attachMask(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.addToBattlefield(player1, new FamiliarGround());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Familiar Ground"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new AncestralMask()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent attachMask(Permanent creature) {
        Permanent mask = new Permanent(new AncestralMask());
        mask.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(mask);
        return mask;
    }
}
