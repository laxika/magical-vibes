package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SultaiFlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 4 life when a creature you control with toughness 4 dies")
    void gainsLifeWhenToughCreatureDies() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.setLife(player1, 20);

        destroy(player1, harness.getPermanentId(player1, "Giant Spider"));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Does not trigger when a creature you control has toughness below 4")
    void doesNotTriggerForLowerToughnessCreature() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        destroy(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Gains 4 life when Sultai Flayer itself dies")
    void gainsLifeWhenItselfDies() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.setLife(player1, 20);

        destroy(player1, harness.getPermanentId(player1, "Sultai Flayer"));

        harness.assertLife(player1, 24);
    }

    private void destroy(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
