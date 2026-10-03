package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoatiScavenger.class, GrizzlyBears.class, LightningBolt.class})
class CoatiScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted permanent card when you have descend 4")
    void returnsPermanentCardWithFourPermanentCardsInGraveyard() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        GrizzlyBears fourth = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, second, third, fourth));

        castCoatiScavenger();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                target.getId(), second.getId(), third.getId(), fourth.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(second, third, fourth);
    }

    @Test
    @DisplayName("ETB does not trigger with fewer than four permanent cards in the graveyard")
    void doesNotTriggerWithFewerThanFourPermanentCards() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, new GrizzlyBears(), new GrizzlyBears()));

        castCoatiScavenger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("ETB targets only permanent cards")
    void targetsOnlyPermanentCards() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        GrizzlyBears fourth = new GrizzlyBears();
        Card nonPermanent = new LightningBolt();
        harness.setGraveyard(player1, List.of(target, second, third, fourth, nonPermanent));

        castCoatiScavenger();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                target.getId(), second.getId(), third.getId(), fourth.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonPermanent);
    }

    @Test
    @DisplayName("ETB does nothing if descend 4 is lost before resolution")
    void doesNothingIfThresholdIsLostBeforeResolution() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        GrizzlyBears fourth = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, second, third, fourth));

        castCoatiScavenger();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Nonpermanent cards do not count toward descend 4")
    void nonPermanentCardsDoNotCountTowardThreshold() {
        harness.setGraveyard(player1, List.of(new CoatiScavenger(), new CoatiScavenger(),
                new CoatiScavenger(), new LightningBolt()));
        List<Card> originalGraveyard = List.copyOf(gd.playerGraveyards.get(player1.getId()));

        castCoatiScavenger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(originalGraveyard);
    }

    @Test
    @DisplayName("Opponent's permanent cards do not count toward descend 4")
    void opponentsGraveyardDoesNotCountTowardThreshold() {
        harness.setGraveyard(player1, List.of(new CoatiScavenger(), new CoatiScavenger(),
                new CoatiScavenger()));
        harness.setGraveyard(player2, List.of(new CoatiScavenger(), new CoatiScavenger(),
                new CoatiScavenger(), new CoatiScavenger()));

        castCoatiScavenger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("ETB cannot target permanent cards in the opponent's graveyard")
    void cannotTargetOpponentsPermanentCards() {
        CoatiScavenger target = new CoatiScavenger();
        CoatiScavenger second = new CoatiScavenger();
        CoatiScavenger third = new CoatiScavenger();
        CoatiScavenger fourth = new CoatiScavenger();
        CoatiScavenger opposingCard = new CoatiScavenger();
        harness.setGraveyard(player1, List.of(target, second, third, fourth));
        harness.setGraveyard(player2, List.of(opposingCard));

        castCoatiScavenger();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                target.getId(), second.getId(), third.getId(), fourth.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
    }

    @Test
    @DisplayName("A missing target is not replaced even when descend 4 remains satisfied")
    void doesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        CoatiScavenger target = new CoatiScavenger();
        CoatiScavenger second = new CoatiScavenger();
        CoatiScavenger third = new CoatiScavenger();
        CoatiScavenger fourth = new CoatiScavenger();
        CoatiScavenger fifth = new CoatiScavenger();
        harness.setGraveyard(player1, List.of(target, second, third, fourth, fifth));

        castCoatiScavenger();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second, third, fourth, fifth);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castCoatiScavenger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CoatiScavenger(), "{2}{G}");
        harness.passBothPriorities();
    }
}
