package com.github.laxika.magicalvibes.cards.b;

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

class BanewaspAfflictionTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, its controller loses life equal to its toughness")
    void enchantedCreatureDeathLosesLifeEqualToToughness() {
        // Giant Spider is 2/4 — this proves the loss tracks toughness (4), not power (2).
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent banewasp = new Permanent(new BanewaspAffliction());
        banewasp.setAttachedTo(spider.getId());
        gd.playerBattlefields.get(player1.getId()).add(banewasp);

        int lifeBefore = gd.getLife(player2.getId());

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("The aura's own controller loses the life when it enchants their creature")
    void ownControllerLosesLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent banewasp = new Permanent(new BanewaspAffliction());
        banewasp.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(banewasp);

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
        harness.setHand(player1, List.of(new BanewaspAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
