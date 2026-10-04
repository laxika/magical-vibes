package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FatefulTempest.class, GrizzlyBears.class, RestInPeace.class})
class FatefulTempestTest extends BaseCardTest {

    @Test
    void pastVotesMillAndDealDamageEqualToMilledManaValues() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        cast();

        vote(ChoiceContext.FatefulTempestChoice.PAST, ChoiceContext.FatefulTempestChoice.PAST);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void presentVotesExileCardsWithPermissionUntilNextTurn() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        cast();

        vote(ChoiceContext.FatefulTempestChoice.PRESENT, ChoiceContext.FatefulTempestChoice.PRESENT);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void splitVoteResolvesBothBranches() {
        GrizzlyBears milled = new GrizzlyBears();
        GrizzlyBears exiled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milled, exiled));
        cast();

        vote(ChoiceContext.FatefulTempestChoice.PAST, ChoiceContext.FatefulTempestChoice.PRESENT);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void milledCardsDivertedToExileStillContributeTheirManaValue() {
        harness.addToBattlefield(player2, new RestInPeace());
        FatefulTempest first = new FatefulTempest();
        FatefulTempest second = new FatefulTempest();
        harness.setLibrary(player1, List.of(first, second));
        cast();

        vote(ChoiceContext.FatefulTempestChoice.PAST, ChoiceContext.FatefulTempestChoice.PAST);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
        assertThat(gd.exilePlayPermissions).doesNotContainKeys(first.getId(), second.getId());
    }

    @Test
    void pastVotesWithOnlyOneCardDealDamageForOnlyThatCard() {
        FatefulTempest milled = new FatefulTempest();
        harness.setLibrary(player1, List.of(milled));
        cast();

        vote(ChoiceContext.FatefulTempestChoice.PAST, ChoiceContext.FatefulTempestChoice.PAST);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    void pastBranchConsumesLastCardBeforePresentBranchEvenWhenPresentIsVotedFirst() {
        FatefulTempest milled = new FatefulTempest();
        harness.setLibrary(player1, List.of(milled));
        cast();

        vote(ChoiceContext.FatefulTempestChoice.PRESENT, ChoiceContext.FatefulTempestChoice.PAST);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player2, 17);
    }

    @Test
    void emptyLibraryDoesNotDealDamageOrExileCards() {
        harness.setLibrary(player1, List.of());
        cast();

        vote(ChoiceContext.FatefulTempestChoice.PAST, ChoiceContext.FatefulTempestChoice.PRESENT);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void controllerCanCastPresentCardByPayingItsManaCost() {
        GrizzlyBears exiled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiled));
        cast();
        vote(ChoiceContext.FatefulTempestChoice.PRESENT, ChoiceContext.FatefulTempestChoice.PRESENT);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiled);
    }

    @Test
    void presentPermissionLastsThroughControllersNextTurnThenExpires() {
        FatefulTempest first = new FatefulTempest();
        FatefulTempest second = new FatefulTempest();
        harness.setLibrary(player1, List.of(first, second,
                new FatefulTempest(), new FatefulTempest(), new FatefulTempest()));
        harness.setLibrary(player2, List.of(new FatefulTempest(), new FatefulTempest()));
        cast();
        vote(ChoiceContext.FatefulTempestChoice.PRESENT, ChoiceContext.FatefulTempestChoice.PRESENT);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsKeys(first.getId(), second.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsKeys(first.getId(), second.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKeys(first.getId(), second.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
    }

    private void cast() {
        harness.castFromHand(player1, new FatefulTempest(), "{2}{R}");
        harness.passBothPriorities();
    }

    private void vote(String firstVote, String secondVote) {
        harness.handleListChoice(player1, firstVote);
        harness.handleListChoice(player2, secondVote);
    }
}
