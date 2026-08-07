package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GallowbraidTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep costs 1 life per age counter")
    void paysCumulativeUpkeepInLife() {
        Permanent gallowbraid = harness.addToBattlefieldAndReturn(player1, new Gallowbraid());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gallowbraid.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gallowbraid);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Third upkeep costs 3 life")
    void thirdUpkeepCostsThreeLife() {
        Permanent gallowbraid = harness.addToBattlefieldAndReturn(player1, new Gallowbraid());
        gallowbraid.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gallowbraid.getCounterCount(CounterType.AGE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gallowbraid);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices Gallowbraid")
    void decliningUpkeepSacrifices() {
        Permanent gallowbraid = harness.addToBattlefieldAndReturn(player1, new Gallowbraid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gallowbraid);
        harness.assertInGraveyard(player1, "Gallowbraid");
    }
}
