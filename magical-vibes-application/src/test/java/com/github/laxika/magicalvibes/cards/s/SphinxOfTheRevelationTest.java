package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SphinxOfTheRevelation.class)
class SphinxOfTheRevelationTest extends BaseCardTest {

    @Test
    void gainsEnergyEqualToLifeGained() {
        addCreatureReady(player1, new SphinxOfTheRevelation());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void paysEnergyAndDrawsChosenAmount() {
        Permanent sphinx = addCreatureReady(player1, new SphinxOfTheRevelation());
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.setLibrary(player1, List.of(
                new SphinxOfTheRevelation(), new SphinxOfTheRevelation(), new SphinxOfTheRevelation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(sphinx.isTapped()).isTrue();
    }

    @Test
    void cannotPayMoreEnergyThanAvailable() {
        Permanent sphinx = addCreatureReady(player1, new SphinxOfTheRevelation());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough energy");
        assertThat(sphinx.isTapped()).isFalse();
    }
}
