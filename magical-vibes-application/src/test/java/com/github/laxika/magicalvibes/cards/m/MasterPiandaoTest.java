package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AangTheLastAirbender;
import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.g.GliderStaff;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
        GrizzlyBears.class
})
class MasterPiandaoTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking reveals eligible Ally, Equipment, and Lesson cards")
    void attackingOffersEligibleCardTypes() {
        GrizzlyBears nonEligible = new GrizzlyBears();
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
        List<Card> topCards = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        setLibrary(topCards);
        addReadyPiandao();

        declarePiandaoAttacking();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }

    private void addReadyPiandao() {
        Permanent piandao = new Permanent(new MasterPiandao());
        piandao.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(piandao);
    }

    private void declarePiandaoAttacking() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }
}
