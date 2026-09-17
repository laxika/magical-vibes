package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BranchsnapLorian;
import com.github.laxika.magicalvibes.cards.m.MacetailHystrodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AphettoExterminator.class, MacetailHystrodon.class, BranchsnapLorian.class})
class AphettoExterminatorTest extends BaseCardTest {

    @Test
    void turningFaceUpGivesTargetCreatureMinusThreeMinusThreeUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MacetailHystrodon());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new MacetailHystrodon());
        Permanent exterminator = castFaceDown();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(exterminator));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(target.getId(), other.getId(), exterminator.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(other.getEffectivePower()).isEqualTo(4);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
        assertThat(exterminator.getEffectivePower()).isEqualTo(3);
        assertThat(exterminator.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(other.getEffectivePower()).isEqualTo(4);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void turningFaceUpPutsTargetWithLethalToughnessReductionIntoGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BranchsnapLorian());
        Permanent exterminator = castFaceDown();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(exterminator));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Branchsnap Lorian");
        harness.assertInGraveyard(player2, "Branchsnap Lorian");
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new AphettoExterminator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Aphetto Exterminator");
    }
}
