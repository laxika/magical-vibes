package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlamespeakerAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Scrying gives Flamespeaker Adept +2/+0 and first strike")
    void scryTriggersBoostAndFirstStrike() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new FlamespeakerAdept());
        scryWithOpt(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, adept, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The scry bonus wears off at end of turn")
    void scryBonusWearsOffAtEndOfTurn() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new FlamespeakerAdept());
        scryWithOpt(player1);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adept)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, adept, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's scry does not trigger Flamespeaker Adept")
    void opponentsScryDoesNotTrigger() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new FlamespeakerAdept());
        scryWithOpt(player2);

        harness.getGameService().handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, adept)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, adept, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void scryWithOpt(Player player) {
        harness.setLibrary(player, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player, List.of(new Opt()));
        harness.addMana(player, ManaColor.BLUE, 1);
        if (player.equals(player2)) {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
        }
        harness.castInstant(player, 0);
        harness.passBothPriorities();
    }
}
