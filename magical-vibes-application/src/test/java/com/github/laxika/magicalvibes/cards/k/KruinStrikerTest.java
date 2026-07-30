package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KruinStrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control entering gives it +1/+0 and trample")
    void pumpsAndGrantsTrampleOnAllyEnter() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new KruinStriker());

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // creature spell resolves -> enters, Striker triggers
        harness.passBothPriorities(); // resolve the boost trigger
        harness.passBothPriorities(); // resolve the trample trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, striker)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new KruinStriker());

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger it")
    void noTriggerForOpponentCreature() {
        Permanent striker = harness.addToBattlefieldAndReturn(player1, new KruinStriker());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, striker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, striker, Keyword.TRAMPLE)).isFalse();
    }
}
