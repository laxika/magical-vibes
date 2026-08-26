package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Swelter.class, GiantSpider.class, GrizzlyBears.class, Mountain.class})
class SwelterTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Deals 2 damage to each of two target creatures")
    void damagesBothTargets() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent otherSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Swelter()));
        giveMana();

        harness.castSorcery(player1, 0, List.of(spider.getId(), otherSpider.getId()));
        harness.passBothPriorities();

        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        assertThat(otherSpider.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Giant Spider"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Requires exactly two creature targets")
    void requiresTwoCreatureTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Swelter()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.setHand(player1, List.of(new Swelter()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId(), mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
