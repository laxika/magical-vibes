package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConclaveMentorTest extends BaseCardTest {

    @Test
    void addsAnExtraPlusOneCounter() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.addToBattlefield(player1, new ConclaveMentor());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(delver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void gainsLifeEqualToItsPowerWhenItDies() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new ConclaveMentor());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mentor);
    }

    @Test
    void usesItsLastKnownPowerWhenItDies() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new ConclaveMentor());
        mentor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }
}
