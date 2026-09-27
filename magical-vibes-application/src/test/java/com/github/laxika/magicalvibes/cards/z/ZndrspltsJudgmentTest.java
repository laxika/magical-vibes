package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZndrspltsJudgment.class, GrizzlyBears.class})
class ZndrspltsJudgmentTest extends BaseCardTest {

    @Test
    void friendCreatesCopyAndFoeReturnsCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castJudgment();

        PendingInteraction.ColorChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());

        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FRIEND);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.ZndrsplatsJudgmentChoice.FOE);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void friendChoosesWhichCreatureToCopy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castJudgment();

        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FRIEND);
        harness.handleListChoice(player2, ChoiceContext.ZndrsplatsJudgmentChoice.FOE);

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.playerId()).isEqualTo(player1.getId());
        assertThat(creatureChoice.validIds()).contains(first.getId());
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(3);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }

    private void castJudgment() {
        harness.castFromHand(player1, new ZndrspltsJudgment(), "{4}{U}");
        harness.passBothPriorities();
    }
}
