package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({BeholdTheUnspeakable.class, VisionOfTheUnspeakable.class, GrizzlyBears.class, Shock.class})
class BeholdTheUnspeakableTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I gives creatures you do not control -2/-0 until your next turn")
    void chapterIWeakensCreaturesYouDoNotControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chapter II scries two and draws two with more than one card in hand")
    void chapterIIScryAndDrawBranch() {
        addSagaWithLore(1);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));

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
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        BeholdTheUnspeakable front = new BeholdTheUnspeakable();
        Permanent vision = new Permanent(front);
        vision.setCard(front.getBackFaceCard());
        vision.setTransformed(true);
        vision.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(vision);

        assertThat(gqs.getEffectivePower(gd, vision)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vision)).isEqualTo(3);
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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
