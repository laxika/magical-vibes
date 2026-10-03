package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrossroadsWatcher.class, FugitiveWizard.class})
class CrossroadsWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each other creature you control that enters")
    void boostsForEachAllyCreatureEntering() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new CrossroadsWatcher());

        harness.setHand(player1, List.of(new FugitiveWizard(), new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature entering")
    void doesNotBoostForOpponentCreatureEntering() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new CrossroadsWatcher());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when it enters itself")
    void doesNotBoostForItsOwnEntry() {
        harness.setHand(player1, List.of(new CrossroadsWatcher()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent watcher = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each existing Watcher boosts itself when another Watcher enters")
    void eachWatcherBoostsOnlyItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CrossroadsWatcher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CrossroadsWatcher());
        harness.setHand(player1, List.of(new CrossroadsWatcher()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        resolveAllTriggers();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(3);
    }
}
