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
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FOE);

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
        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FOE);

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.playerId()).isEqualTo(player1.getId());
        assertThat(creatureChoice.validIds()).contains(first.getId());
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(3);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }

    @Test
    void controllerCanDesignateBothPlayersAsFriends() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castJudgment();

        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FRIEND);
        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FRIEND);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(2);
    }

    @Test
    void controllerCanDesignateBothPlayersAsFoes() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castJudgment();

        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FOE);
        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FOE);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void playersWithoutCreaturesStillReceiveDesignations() {
        castJudgment();

        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FRIEND);
        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FOE);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Zndrsplt's Judgment");
    }

    @Test
    void friendFinishesCopyingBeforeFoeChoosesCreature() {
        Permanent foeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent friendCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castJudgment();

        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FOE);
        harness.handleListChoice(player1, ChoiceContext.ZndrsplatsJudgmentChoice.FRIEND);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, friendCreature.getId());

        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(3);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handlePermanentChosen(player1, foeCreature.getId());

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castJudgment() {
        harness.castFromHand(player1, new ZndrspltsJudgment(), "{4}{U}");
        harness.passBothPriorities();
    }
}
