package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.y.YuyanArchers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterPakku.class, AirbendingLesson.class, YuyanArchers.class})
class MasterPakkuTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped mills target player for each Lesson in its controller's graveyard")
    void becomingTappedMillsForLessonsInControllerGraveyard() {
        Permanent pakku = harness.addToBattlefieldAndReturn(player1, new MasterPakku());
        harness.setGraveyard(player1, List.of(
                new AirbendingLesson(), new AirbendingLesson(), new YuyanArchers()));
        harness.setGraveyard(player2, List.of(new AirbendingLesson()));
        harness.setLibrary(player2, libraryWithFiveCards());

        tap(pakku);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Tapping another permanent does not trigger Master Pakku")
    void tappingAnotherPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new MasterPakku());
        Permanent archers = harness.addToBattlefieldAndReturn(player1, new YuyanArchers());

        tap(archers);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void lessonCountIsDeterminedAtResolution() {
        Permanent pakku = harness.addToBattlefieldAndReturn(player1, new MasterPakku());
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        harness.setLibrary(player2, libraryWithFiveCards());

        tap(pakku);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void canTargetControllerAndMillsOnlyAvailableCards() {
        Permanent pakku = harness.addToBattlefieldAndReturn(player1, new MasterPakku());
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new YuyanArchers()));

        tap(pakku);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void stillTargetsButMillsNothingWithoutLessons() {
        Permanent pakku = harness.addToBattlefieldAndReturn(player1, new MasterPakku());
        harness.setGraveyard(player1, List.of(new YuyanArchers()));
        harness.setGraveyard(player2, List.of(new AirbendingLesson()));
        harness.setLibrary(player2, libraryWithFiveCards());

        tap(pakku);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void noncreatureSpellBoostsPakkuUntilEndOfTurn() {
        Permanent pakku = harness.addToBattlefieldAndReturn(player1, new MasterPakku());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YuyanArchers());
        harness.setLibrary(player1, libraryWithFiveCards());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(pakku.getPowerModifier()).isEqualTo(1);
        assertThat(pakku.getToughnessModifier()).isEqualTo(1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pakku.getPowerModifier()).isZero();
        assertThat(pakku.getToughnessModifier()).isZero();
    }

    @Test
    void creatureSpellDoesNotTriggerProwess() {
        harness.addToBattlefield(player1, new MasterPakku());

        harness.castFromHand(player1, new YuyanArchers(), "{1}{R}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentNoncreatureSpellDoesNotTriggerProwess() {
        Permanent pakku = harness.addToBattlefieldAndReturn(player1, new MasterPakku());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YuyanArchers());
        harness.setHand(player2, List.of(new AirbendingLesson()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(pakku.getPowerModifier()).isZero();
        assertThat(pakku.getToughnessModifier()).isZero();
    }

    @Test
    void attackingTriggersMillThroughCombat() {
        Permanent pakku = harness.addToBattlefieldAndReturn(player1, new MasterPakku());
        pakku.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(
                new YuyanArchers(),
                new YuyanArchers(),
                new YuyanArchers(),
                new YuyanArchers(),
                new YuyanArchers());
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
            harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd);
        });
    }
}
