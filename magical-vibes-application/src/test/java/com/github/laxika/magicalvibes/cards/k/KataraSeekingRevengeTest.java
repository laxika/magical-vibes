package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WaterbendingLesson;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

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

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(firstSource.getId(), secondSource.getId()), List.of(), false,
                null, null, null, null, null, null, true);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        assertThat(firstSource.isTapped()).isTrue();
        assertThat(secondSource.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNull();
    }

    @Test
    @DisplayName("Can pay the optional waterbend cost entirely with mana and cast with black")
    void paysWaterbendWithManaOnly() {
        harness.setHand(player1, List.of(new KataraSeekingRevenge()));
        WaterbendingLesson drawnCard = new WaterbendingLesson();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(), List.of(), false, null, null, null, null, null, null, true);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Katara, Seeking Revenge");
    }

    @Test
    @DisplayName("Without waterbend, can discard the card just drawn and immediately count it as a Lesson")
    void discardsDrawnLessonFromInitiallyEmptyHand() {
        harness.setHand(player1, List.of(new KataraSeekingRevenge()));
        WaterbendingLesson drawnCard = new WaterbendingLesson();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setGraveyard(player1, List.of());
        addMana();

        harness.castCreature(player1, 0);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        Permanent katara = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(4);
    }

    @Test
    @DisplayName("Continuously updates the bonus as Lessons enter and leave the graveyard")
    void updatesBonusWhenGraveyardChanges() {
        Permanent katara = addCreatureReady(player1, new KataraSeekingRevenge());
        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(new WaterbendingLesson(), new WaterbendingLesson()));
        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(5);

        harness.setGraveyard(player1, List.of(new WaterbendingLesson()));
        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(4);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, katara)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, katara)).isEqualTo(3);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
