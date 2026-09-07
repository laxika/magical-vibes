package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    private void castCoatiScavenger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CoatiScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
