package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TorchSongTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may put a verse counter on Torch Song")
    void upkeepTriggerAddsVerseCounter() {
        Permanent torchSong = harness.addToBattlefieldAndReturn(player1, new TorchSong());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(torchSong.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves Torch Song unchanged")
    void decliningUpkeepTriggerDoesNothing() {
        Permanent torchSong = harness.addToBattlefieldAndReturn(player1, new TorchSong());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(torchSong.getCounterCount(CounterType.VERSE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrificing Torch Song deals damage equal to its verse counters")
    void sacrificeDealsDamageEqualToVerseCounters() {
        Permanent torchSong = harness.addToBattlefieldAndReturn(player1, new TorchSong());
        torchSong.setCounterCount(CounterType.VERSE, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(torchSong);
        harness.assertInGraveyard(player1, "Torch Song");
    }
}
