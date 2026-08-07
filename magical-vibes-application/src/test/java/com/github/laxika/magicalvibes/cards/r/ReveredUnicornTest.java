package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReveredUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices it and gains life equal to its age counters")
    void decliningUpkeepSacrificesAndGainsLife() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new ReveredUnicorn());
        unicorn.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // The third age counter goes on before the cost is sized.
        assertThat(unicorn.getCounterCount(CounterType.AGE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(unicorn);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Paying the cumulative upkeep keeps it on the battlefield and gains no life")
    void payingUpkeepGainsNoLife() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new ReveredUnicorn());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unicorn);
        harness.assertLife(player1, 20);
    }
}
