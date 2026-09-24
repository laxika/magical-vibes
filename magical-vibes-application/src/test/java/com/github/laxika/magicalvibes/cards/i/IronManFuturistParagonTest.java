package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LevitatingStatue;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManFuturistParagon.class, GrizzlyBears.class, LevitatingStatue.class, Forest.class})
class IronManFuturistParagonTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat animates a target artifact or creature permanently")
    void animatesTargetArtifactOrCreaturePermanently() {
        Permanent ironMan = harness.addToBattlefieldAndReturn(player1, new IronManFuturistParagon());
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent targetArtifact = harness.addToBattlefieldAndReturn(player1, new LevitatingStatue());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                ironMan.getId(), targetCreature.getId(), targetArtifact.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, targetCreature)).isTrue();
        assertThat(gqs.isArtifact(targetCreature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, targetCreature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The animation remains after cleanup and cannot target a land")
    void animationIsPermanentAndLandIsIllegalTarget() {
        Permanent ironMan = harness.addToBattlefieldAndReturn(player1, new IronManFuturistParagon());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId(), ironMan.getId())
                .doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
