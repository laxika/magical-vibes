package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrivateEye.class, NoviceInspector.class, GrizzlyBears.class})
class PrivateEyeTest extends BaseCardTest {

    @Test
    void boostsOtherDetectivesYouControl() {
        Permanent detective = harness.addToBattlefieldAndReturn(player1, new NoviceInspector());
        Permanent nonDetective = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        int detectivePower = gqs.getEffectivePower(gd, detective);
        int detectiveToughness = gqs.getEffectiveToughness(gd, detective);
        int nonDetectivePower = gqs.getEffectivePower(gd, nonDetective);
        int nonDetectiveToughness = gqs.getEffectiveToughness(gd, nonDetective);
        harness.addToBattlefield(player1, new PrivateEye());

        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(detectivePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(detectiveToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonDetective)).isEqualTo(nonDetectivePower);
        assertThat(gqs.getEffectiveToughness(gd, nonDetective)).isEqualTo(nonDetectiveToughness);
    }

    @Test
    void makesAChosenDetectiveUnblockableAfterSecondDraw() {
        harness.addToBattlefield(player1, new PrivateEye());
        Permanent opponentDetective = harness.addToBattlefieldAndReturn(player2, new NoviceInspector());
        Permanent opponentNonDetective = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .contains(opponentDetective.getId())
                .doesNotContain(opponentNonDetective.getId());

        harness.handlePermanentChosen(player1, opponentDetective.getId());
        harness.passBothPriorities();

        assertThat(opponentDetective.isCantBeBlocked()).isTrue();
    }

    private void drawCard(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
