package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WaterbendingLesson;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KataraSeekingRevenge.class, GrizzlyBears.class, WaterbendingLesson.class})
class KataraSeekingRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each Lesson card in its controller's graveyard")
    void boostsForLessonsInOwnGraveyard() {
        Permanent katara = addCreatureReady(player1, new KataraSeekingRevenge());
        harness.setGraveyard(player1, List.of(
                new WaterbendingLesson(), new WaterbendingLesson(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new WaterbendingLesson()));

        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(5);
    }

    @Test
    @DisplayName("Draws then discards when the waterbend cost was not paid")
    void drawsThenDiscardsWithoutWaterbend() {
        harness.setHand(player1, List.of(new KataraSeekingRevenge(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not discard when the waterbend cost was paid")
    void drawsWithoutDiscardingWithWaterbend() {
        Permanent firstSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KataraSeekingRevenge()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castCreatureTappingPermanents(player1, 0,
                List.of(firstSource.getId(), secondSource.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstSource.isTapped()).isTrue();
        assertThat(secondSource.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
