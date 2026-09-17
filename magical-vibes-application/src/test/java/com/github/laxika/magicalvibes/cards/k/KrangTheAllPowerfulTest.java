package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrangTheAllPowerful.class, GrizzlyBears.class})
class KrangTheAllPowerfulTest extends BaseCardTest {

    @Test
    void doublesItsSecondDrawTriggerForItsController() {
        Permanent krang = harness.addToBattlefieldAndReturn(player1, new KrangTheAllPowerful());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(krang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doublesItsSecondDrawTriggerForAnOpponent() {
        Permanent krang = harness.addToBattlefieldAndReturn(player1, new KrangTheAllPowerful());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        draw(player2);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(krang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
