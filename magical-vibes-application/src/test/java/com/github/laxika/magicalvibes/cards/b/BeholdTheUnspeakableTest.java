package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.c.ClawingTorment;
import com.github.laxika.magicalvibes.cards.v.VisionOfTheUnspeakable;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeholdTheUnspeakable.class, VisionOfTheUnspeakable.class, JukaiTrainee.class, ClawingTorment.class})
class BeholdTheUnspeakableTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I gives creatures you do not control -2/-0 until your next turn")
    void chapterIWeakensCreaturesYouDoNotControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter II draws four cards with one or fewer cards in hand")
    void chapterIIFourCardBranch() {
        addSagaWithLore(1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ClawingTorment(), new ClawingTorment(), new ClawingTorment(), new ClawingTorment(), new ClawingTorment()));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chapter II scries two and draws two with more than one card in hand")
    void chapterIIScryAndDrawBranch() {
        addSagaWithLore(1);
        harness.setHand(player1, List.of(new ClawingTorment(), new ClawingTorment()));
        harness.setLibrary(player1, List.of(new ClawingTorment(), new ClawingTorment(), new ClawingTorment(), new ClawingTorment(), new ClawingTorment()));

        advanceToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Chapter III exiles the Saga and returns it transformed")
    void chapterIIITransformsIntoVision() {
        harness.setHand(player1, List.of(new ClawingTorment()));
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent vision = findPermanent(player1, "Vision of the Unspeakable");
        assertThat(vision).isNotNull();
        assertThat(vision.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Vision of the Unspeakable gets +1/+1 for each card in its controller's hand")
    void visionGetsBiggerWithCardsInHand() {
        harness.setHand(player1, List.of(new ClawingTorment(), new ClawingTorment(), new ClawingTorment()));
        BeholdTheUnspeakable front = new BeholdTheUnspeakable();
        Permanent vision = harness.addToBattlefieldAndReturn(player1, front);
        vision.setCard(front.getBackFaceCard());
        vision.setTransformed(true);

        assertThat(gqs.getEffectivePower(gd, vision)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vision)).isEqualTo(3);
    }

    @Test
    void chapterIIDrawsAfterApplyingScryOrder() {
        addSagaWithLore(1);
        harness.setHand(player1, List.of(new ClawingTorment(), new ClawingTorment()));
        JukaiTrainee bottomed = new JukaiTrainee();
        ClawingTorment kept = new ClawingTorment();
        JukaiTrainee next = new JukaiTrainee();
        harness.setLibrary(player1, List.of(bottomed, kept, next));

        advanceToNextChapter();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(kept, next).doesNotContain(bottomed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomed);
    }

    @Test
    void chapterIIDrawsFourWithExactlyOneCardAtResolution() {
        addSagaWithLore(1);
        harness.setHand(player1, List.of(new ClawingTorment(), new ClawingTorment()));
        harness.setLibrary(player1, List.of(new ClawingTorment(), new ClawingTorment(), new ClawingTorment(), new ClawingTorment()));

        advanceToNextChapter();
        harness.setHand(player1, List.of(new ClawingTorment()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void chapterIAffectsOnlyCreaturesPresentAtResolutionAndExpiresNextTurn() {
        harness.setHand(player2, List.of());
        Permanent affected = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());
        harness.setLibrary(player2, List.of(new ClawingTorment(), new ClawingTorment()));
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, affected)).isZero();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, affected)).isEqualTo(2);
    }

    @Test
    void chapterIIIReturnsUnderChapterControllersControlRatherThanOwnersControl() {
        harness.setHand(player1, List.of(new ClawingTorment(), new ClawingTorment()));
        harness.setHand(player2, List.of(new ClawingTorment()));
        BeholdTheUnspeakable card = new BeholdTheUnspeakable();
        card.setOwnerId(player2.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, card);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vision of the Unspeakable");
        harness.assertNotOnBattlefield(player2, "Vision of the Unspeakable");
    }

    @Test
    void returnedVisionCannotAttackUntilItsControllersNextTurn() {
        harness.setHand(player1, List.of(new ClawingTorment()));
        addSagaWithLore(2);
        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent vision = findPermanent(player1, "Vision of the Unspeakable");
        assertThat(vision).isNotNull();
        assertThat(harness.getAttackLegalityService().canAttack(gd, vision, player1.getId())).isFalse();
        harness.performUntapStep(player1);
        assertThat(harness.getAttackLegalityService().canAttack(gd, vision, player1.getId())).isTrue();
    }

    @Test
    void transformedVisionTracksHandChangesAndDiesWithAnEmptyHand() {
        harness.setHand(player1, List.of(new ClawingTorment(), new ClawingTorment(), new ClawingTorment()));
        addSagaWithLore(2);
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent vision = findPermanent(player1, "Vision of the Unspeakable");
        assertThat(vision).isNotNull();
        assertThat(gqs.getEffectivePower(gd, vision)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vision)).isEqualTo(3);

        harness.setHand(player1, List.of(new ClawingTorment()));
        assertThat(gqs.getEffectivePower(gd, vision)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vision)).isEqualTo(1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vision of the Unspeakable");
        harness.assertInGraveyard(player1, "Behold the Unspeakable");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BeholdTheUnspeakable());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }
}
