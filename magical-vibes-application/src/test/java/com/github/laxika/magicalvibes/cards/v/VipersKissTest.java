package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VipersKissTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -1/-1")
    void enchantedCreatureGetsMinusOneMinusOne() {
        Permanent creature = new Permanent(new HillGiant());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new VipersKiss()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        ProdigalPyromancer pyromancerCard = new ProdigalPyromancer();
        Permanent pyromancer = new Permanent(pyromancerCard);
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancer);

        Permanent aura = new Permanent(new VipersKiss());
        aura.setAttachedTo(pyromancer.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        harness.setHand(player1, List.of(new VipersKiss()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
