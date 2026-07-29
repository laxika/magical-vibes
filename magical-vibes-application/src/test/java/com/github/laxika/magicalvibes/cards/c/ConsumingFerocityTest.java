package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsumingFerocityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+0")
    void enchantedCreatureGetsBoost() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new ConsumingFerocity());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep puts a +1/+0 counter on the enchanted creature without killing it")
    void upkeepPutsCounter() {
        Permanent creature = castOnGrizzlyBears();

        runUpkeep();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(1);
        // Base 2/2 + aura +1/+0 + one +1/+0 counter.
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Third counter makes the creature damage its controller for its power and die")
    void thirdCounterDestroysCreature() {
        Permanent creature = castOnGrizzlyBears();

        runUpkeep();
        runUpkeep();
        runUpkeep();

        // Power at the third trigger: base 2 + aura 1 + three +1/+0 counters = 6.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting("name").contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot enchant a Wall")
    void cannotEnchantWall() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new WallOfAir());
        harness.setHand(player1, List.of(new ConsumingFerocity()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent wall = findPermanent(player1, "Wall of Air");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Wall creature");
    }

    private Permanent castOnGrizzlyBears() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ConsumingFerocity()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private void runUpkeep() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
