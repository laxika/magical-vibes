package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AangTheLastAirbender;
import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.g.GliderStaff;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        MasterPiandao.class,
        AangTheLastAirbender.class,
        GliderStaff.class,
        AirbendingLesson.class,
        Forest.class
})
class MasterPiandaoTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking reveals eligible Ally, Equipment, and Lesson cards")
    void attackingOffersEligibleCardTypes() {
        Forest nonEligible = new Forest();
        AangTheLastAirbender ally = new AangTheLastAirbender();
        GliderStaff equipment = new GliderStaff();
        AirbendingLesson lesson = new AirbendingLesson();
        setLibrary(List.of(nonEligible, ally, equipment, lesson));
        addReadyPiandao();

        declarePiandaoAttacking();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                ally.getId(), equipment.getId(), lesson.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ally.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(ally);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonEligible, equipment, lesson);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible card, attacking puts the top four on the bottom")
    void noEligibleCardIsPutOnBottom() {
        List<Card> topCards = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        setLibrary(topCards);
        addReadyPiandao();

        declarePiandaoAttacking();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    @DisplayName("Declining an eligible card bottoms only the top four")
    void mayDeclineEligibleCard() {
        AangTheLastAirbender ally = new AangTheLastAirbender();
        List<Card> topCards = List.of(ally, new Forest(), new Forest(), new Forest());
        GliderStaff untouched = new GliderStaff();
        harness.setLibrary(player1, List.of(topCards.get(0), topCards.get(1),
                topCards.get(2), topCards.get(3), untouched));
        harness.setHand(player1, List.of());
        addReadyPiandao();

        declarePiandaoAttacking();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Equipment can be chosen from a library with fewer than four cards")
    void choosesEquipmentFromShortLibrary() {
        GliderStaff equipment = new GliderStaff();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, equipment));
        harness.setHand(player1, List.of());
        addReadyPiandao();

        declarePiandaoAttacking();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing a Lesson leaves cards below the top four in order")
    void choosesLessonAndPreservesUntouchedCards() {
        AirbendingLesson lesson = new AirbendingLesson();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        AangTheLastAirbender fifth = new AangTheLastAirbender();
        GliderStaff sixth = new GliderStaff();
        harness.setLibrary(player1, List.of(first, lesson, second, third, fifth, sixth));
        harness.setHand(player1, List.of());
        addReadyPiandao();

        declarePiandaoAttacking();
        harness.passBothPriorities();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(lesson.getId());
        harness.handleMultipleCardsChosen(player1, List.of(lesson.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(fifth, sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 5))
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a draw")
    void emptyLibraryDoesNotPrompt() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());
        addReadyPiandao();

        declarePiandaoAttacking();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setLibrary(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void addReadyPiandao() {
        Permanent piandao = harness.addToBattlefieldAndReturn(player1, new MasterPiandao());
        piandao.setSummoningSick(false);
    }

    private void declarePiandaoAttacking() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }
}
