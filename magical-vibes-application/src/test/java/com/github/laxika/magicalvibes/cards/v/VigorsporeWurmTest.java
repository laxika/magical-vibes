package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VigorsporeWurmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +X/+X and vigilance for creature cards in your graveyard")
    void etbBoostsTargetByOwnCreatureCardsInGraveyard() {
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new VigorsporeWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        gs.playCard(gd, player1, 0, 0, target.getId(), null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.getGrantedKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("ETB boost and vigilance wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new VigorsporeWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Vigorspore Wurm cannot be blocked by more than one creature")
    void cannotBeBlockedByMoreThanOneCreature() {
        Permanent blockerOne = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blockerTwo = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new VigorsporeWurm());
        wurm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerOneIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne);
        int blockerTwoIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo);
        int wurmIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wurm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerOneIndex, wurmIndex),
                new BlockerAssignment(blockerTwoIndex, wurmIndex))))
                .isInstanceOf(IllegalStateException.class);
    }
}
