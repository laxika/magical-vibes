package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LlanowarStalker.class, FugitiveWizard.class})
class LlanowarStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering under your control gives Llanowar Stalker +1/+0")
    void boostsWhenAnotherCreatureEnters() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new LlanowarStalker());
        int powerBefore = stalker.getEffectivePower();

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(stalker.getEffectivePower()).isEqualTo(powerBefore + 1);
        assertThat(stalker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Llanowar Stalker's own entry does not trigger it")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new LlanowarStalker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent stalker = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(stalker.getEffectivePower()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The +1/+0 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new LlanowarStalker());

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(stalker.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger it")
    void opponentCreatureDoesNotTrigger() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new LlanowarStalker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(stalker.getEffectivePower()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
