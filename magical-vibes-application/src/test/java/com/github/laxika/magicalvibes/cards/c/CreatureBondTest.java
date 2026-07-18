package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreatureBondTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, its controller takes damage equal to its toughness")
    void enchantedCreatureDeathDealsDamageEqualToToughness() {
        // Giant Spider is 2/4 — this proves the damage tracks toughness (4), not power (2).
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent bond = new Permanent(new CreatureBond());
        bond.setAttachedTo(spider.getId());
        gd.playerBattlefields.get(player1.getId()).add(bond);

        int lifeBefore = gd.getLife(player2.getId());

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("The aura's own controller takes the damage when it enchants their creature")
    void ownControllerTakesDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bond = new Permanent(new CreatureBond());
        bond.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(bond);

        int lifeBefore = gd.getLife(player1.getId());

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CreatureBond()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
