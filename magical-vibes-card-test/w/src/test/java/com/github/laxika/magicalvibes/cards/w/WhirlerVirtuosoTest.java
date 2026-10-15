package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhirlerVirtuoso.class})
class WhirlerVirtuosoTest extends BaseCardTest {

    @Test
    void entersWithThreeEnergyCounters() {
        harness.castFromHand(player1, new WhirlerVirtuoso(), "{1}{U}{R}");
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
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getSubtypes())
                .contains(com.github.laxika.magicalvibes.model.CardSubtype.THOPTER);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotActivateWithoutThreeEnergyCounters() {
        Permanent virtuoso = addCreatureReady(player1, new WhirlerVirtuoso());

        int virtuosoIndex = gd.playerBattlefields.get(player1.getId()).indexOf(virtuoso);
        assertThatThrownBy(() -> harness.activateAbility(player1, virtuosoIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }

    @Test
    void energyIsPaidImmediatelyAndAbilitySurvivesSourceLeaving() {
        Permanent virtuoso = addCreatureReady(player1, new WhirlerVirtuoso());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(virtuoso);
        gd.playerBattlefields.get(player1.getId()).remove(virtuoso);
        gd.playerGraveyards.get(player1.getId()).add(virtuoso.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().isToken()).isTrue();
    }

    @Test
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent virtuoso = harness.addToBattlefieldAndReturn(player1, new WhirlerVirtuoso());
        virtuoso.setSummoningSick(true);
        virtuoso.tap();
        gd.playerEnergyCounters.put(player1.getId(), 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).count()).isEqualTo(2);
    }

    @Test
    void twoEnergyCannotPayTheCost() {
        addCreatureReady(player1, new WhirlerVirtuoso());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
