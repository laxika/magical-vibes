package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TorchSong.class, GorillaWarrior.class})
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
    @DisplayName("Torch Song does not trigger during an opponent's upkeep")
    void upkeepTriggerOnlyHappensDuringControllersUpkeep() {
        Permanent torchSong = harness.addToBattlefieldAndReturn(player1, new TorchSong());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(torchSong.getCounterCount(CounterType.VERSE)).isZero();
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

    @Test
    @DisplayName("Sacrificing Torch Song with no verse counters deals zero damage")
    void sacrificeWithNoVerseCountersDealsNoDamage() {
        Permanent torchSong = harness.addToBattlefieldAndReturn(player1, new TorchSong());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(torchSong);
        harness.assertInGraveyard(player1, "Torch Song");
    }

    @Test
    @DisplayName("Torch Song can deal its damage to a creature")
    void sacrificeDealsDamageToCreatureTarget() {
        Permanent torchSong = harness.addToBattlefieldAndReturn(player1, new TorchSong());
        torchSong.setCounterCount(CounterType.VERSE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gorilla Warrior");
        harness.assertInGraveyard(player1, "Torch Song");
    }
}
