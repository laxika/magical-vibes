package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFlux.class, GrizzlyBears.class, Shock.class})
class TheFluxTest extends BaseCardTest {

    @Test
    void chapterIDealsFourDamageToAcreatureAnOpponentControls() {
        GrizzlyBears opponentCard = new GrizzlyBears();
        opponentCard.setToughness(5);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, opponentCard);
        addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void chapterIIExilesTheTopCardAndAllowsPlayingIt() {
        assertChapterExilesTopCardAndAllowsPlaying(1);
    }

    @Test
    void chapterVExilesTheTopCardAndAllowsPlayingIt() {
        assertChapterExilesTopCardAndAllowsPlaying(4);
    }

    @Test
    void chapterVIAddsSixRedManaAndSacrificesTheSaga() {
        Permanent saga = addSagaWithLore(5);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private void assertChapterExilesTopCardAndAllowsPlaying(int loreCounters) {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        addSagaWithLore(loreCounters);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFlux());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
