package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.r.RunawayTrashBot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamiOfIndustry.class, MindStone.class, GildedLotus.class, RunawayTrashBot.class})
class KamiOfIndustryTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a small artifact with haste and sacrifices it at the next end step")
    void returnsSmallArtifactWithHasteAndSacrificesItAtNextEndStep() {
        Card artifact = new MindStone();
        harness.setGraveyard(player1, List.of(artifact));
        harness.castFromHand(player1, new KamiOfIndustry(), "{4}{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Mind Stone");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Mind Stone");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Mind Stone");
    }

    @Test
    @DisplayName("Cannot target an artifact with mana value greater than three")
    void cannotTargetArtifactWithManaValueGreaterThanThree() {
        Card artifact = new GildedLotus();
        harness.setGraveyard(player1, List.of(artifact));
        harness.castFromHand(player1, new KamiOfIndustry(), "{4}{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNull();
        harness.assertInGraveyard(player1, "Gilded Lotus");
        harness.assertNotOnBattlefield(player1, "Gilded Lotus");
    }

    @Test
    void targetsOnlyArtifactsInYourGraveyardAndIncludesManaValueThree() {
        Card artifact = new RunawayTrashBot();
        Card nonartifact = new KamiOfIndustry();
        Card opponentsArtifact = new RunawayTrashBot();
        harness.setGraveyard(player1, List.of(artifact, nonartifact));
        harness.setGraveyard(player2, List.of(opponentsArtifact));

        harness.castFromHand(player1, new KamiOfIndustry(), "{4}{R}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Runaway Trash-Bot");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertInGraveyard(player1, "Kami of Industry");
        harness.assertInGraveyard(player2, "Runaway Trash-Bot");
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        Card artifact = new RunawayTrashBot();
        harness.setGraveyard(player1, List.of(artifact));
        harness.castFromHand(player1, new KamiOfIndustry(), "{4}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runaway Trash-Bot");
        harness.assertOnBattlefield(player1, "Kami of Industry");
    }

    @Test
    void returnAndDelayedSacrificeDoNotDependOnKamiRemainingOnBattlefield() {
        Card artifact = new RunawayTrashBot();
        harness.setGraveyard(player1, List.of(artifact));
        harness.castFromHand(player1, new KamiOfIndustry(), "{4}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().sacrificePermanentToGraveyard(
                gd, findPermanent(player1, "Kami of Industry")));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Runaway Trash-Bot");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runaway Trash-Bot");
        harness.assertInGraveyard(player1, "Runaway Trash-Bot");
    }

    @Test
    void returningDuringEndStepWaitsUntilOpponentsEndStepToSacrifice() {
        Card artifact = new RunawayTrashBot();
        harness.setGraveyard(player1, List.of(artifact));
        harness.forceStep(TurnStep.END_STEP);
        harness.enterBattlefieldAndReturn(player1, new KamiOfIndustry());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runaway Trash-Bot");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runaway Trash-Bot");
        harness.assertInGraveyard(player1, "Runaway Trash-Bot");
    }
}
