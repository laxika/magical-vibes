package com.github.laxika.magicalvibes.cards.d;

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

class DeathWatchTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted creature dies, controller loses life = power and you gain life = toughness")
    void enchantedCreatureDeathDrainsPowerGainsToughness() {
        // Giant Spider is 2/4 — loss tracks power (2), gain tracks toughness (4).
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent deathWatch = new Permanent(new DeathWatch());
        deathWatch.setAttachedTo(spider.getId());
        gd.playerBattlefields.get(player1.getId()).add(deathWatch);

        int p1Before = gd.getLife(player1.getId());
        int p2Before = gd.getLife(player2.getId());

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2Before - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1Before + 4);
    }

    @Test
    @DisplayName("Enchanting your own creature applies both halves to you")
    void ownCreatureBothHalves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent deathWatch = new Permanent(new DeathWatch());
        deathWatch.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(deathWatch);

        int lifeBefore = gd.getLife(player1.getId());

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        // 2/2: lose 2, gain 2 → net zero
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DeathWatch()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
