package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhirlerVirtuosoTest extends BaseCardTest {

    @Test
    void entersWithThreeEnergyCounters() {
        harness.setHand(player1, List.of(new WhirlerVirtuoso()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void paysThreeEnergyToCreateThopterToken() {
        Permanent virtuoso = addCreatureReady(player1, new WhirlerVirtuoso());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        int virtuosoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(virtuoso);
        harness.activateAbility(player1, virtuosoIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        Permanent thopter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    void cannotActivateWithoutThreeEnergyCounters() {
        Permanent virtuoso = addCreatureReady(player1, new WhirlerVirtuoso());

        int virtuosoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(virtuoso);
        assertThatThrownBy(() -> harness.activateAbility(player1, virtuosoIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }
}
