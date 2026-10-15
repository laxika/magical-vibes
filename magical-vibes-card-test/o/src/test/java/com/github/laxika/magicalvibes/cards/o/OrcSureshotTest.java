package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DragonBellMonk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcSureshot.class, DragonBellMonk.class})
class OrcSureshotTest extends BaseCardTest {

    @Test
    @DisplayName("Orc Sureshot does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonBellMonk());

        harness.castFromHand(player1, new OrcSureshot(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each creature entry triggers independently and repeated debuffs can kill a creature")
    void repeatedEntriesKillTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonBellMonk());
        harness.addToBattlefield(player1, new OrcSureshot());

        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new DragonBellMonk(), "{2}{W}");
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player2, "Dragon Bell Monk");
        harness.assertInGraveyard(player2, "Dragon Bell Monk");
    }

    @Test
    @DisplayName("An entry with no opposing creature does not leave a target choice pending")
    void noLegalTargetDoesNotBlockGame() {
        harness.addToBattlefield(player1, new OrcSureshot());

        harness.castFromHand(player1, new DragonBellMonk(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Dragon Bell Monk");
    }

    @Test
    @DisplayName("Another creature entering under its controller's control gives a target opponent creature -1/-1")
    void allyCreatureEnteringShrinksTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonBellMonk());
        harness.addToBattlefield(player1, new OrcSureshot());

        harness.castFromHand(player1, new DragonBellMonk(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DragonBellMonk());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DragonBellMonk());
        harness.addToBattlefield(player1, new OrcSureshot());

        harness.castFromHand(player1, new DragonBellMonk(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonBellMonk());
        harness.addToBattlefield(player1, new OrcSureshot());

        harness.castFromHand(player1, new DragonBellMonk(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability does not trigger when an opponent's creature enters")
    void opponentCreatureEnteringDoesNotTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DragonBellMonk());
        harness.addToBattlefield(player1, new OrcSureshot());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new DragonBellMonk(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
