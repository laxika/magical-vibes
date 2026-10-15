package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BiogenicOoze;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrangTheAllPowerful.class, BiogenicOoze.class})
class KrangTheAllPowerfulTest extends BaseCardTest {

    @Test
    void doublesItsSecondDrawTriggerForItsController() {
        Permanent krang = harness.addToBattlefieldAndReturn(player1, new KrangTheAllPowerful());
        harness.setLibrary(player1, List.of(new KrangTheAllPowerful(), new KrangTheAllPowerful()));

        draw(player1);
        draw(player1);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(krang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doublesItsSecondDrawTriggerForAnOpponent() {
        Permanent krang = harness.addToBattlefieldAndReturn(player1, new KrangTheAllPowerful());
        harness.setLibrary(player2, List.of(new KrangTheAllPowerful(), new KrangTheAllPowerful()));

        draw(player2);
        draw(player2);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(krang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotDoubleEnterTheBattlefieldTriggers() {
        harness.addToBattlefield(player1, new KrangTheAllPowerful());

        harness.enterBattlefieldAndReturn(player1, new BiogenicOoze());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Ooze")).hasSize(1);
    }

    @Test
    void countsEachPlayersDrawsSeparatelyAndOnlyTriggersOnTheirSecondDraw() {
        Permanent krang = harness.addToBattlefieldAndReturn(player1, new KrangTheAllPowerful());
        harness.setLibrary(player1, List.of(new KrangTheAllPowerful(), new KrangTheAllPowerful(),
                new KrangTheAllPowerful()));
        harness.setLibrary(player2, List.of(new KrangTheAllPowerful(), new KrangTheAllPowerful(),
                new KrangTheAllPowerful()));

        draw(player1);
        draw(player2);
        assertThat(gd.stack).isEmpty();

        draw(player1);
        draw(player2);
        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();
        assertThat(krang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        draw(player1);
        draw(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(krang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
