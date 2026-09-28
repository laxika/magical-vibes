package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FatefulTempest.class, GrizzlyBears.class})
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

    private void cast() {
        harness.setHand(player1, List.of(new FatefulTempest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private void vote(String firstVote, String secondVote) {
        harness.handleListChoice(player1, firstVote);
        harness.handleListChoice(player2, secondVote);
    }
}
