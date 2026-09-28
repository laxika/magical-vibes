package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaitSithFortuneTeller.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class CaitSithFortuneTellerTest extends BaseCardTest {

    @Test
    void scriesThenExilesTopCardAndBoostsTargetByItsManaValue() {
        harness.addToBattlefield(player1, new CaitSithFortuneTeller());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        HillGiant topCard = new HillGiant();
        harness.setLibrary(player1, List.of(topCard, new Forest()));

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(target.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void scryingToBottomExilesTheNextCard() {
        harness.addToBattlefield(player1, new CaitSithFortuneTeller());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest scryCard = new Forest();
        HillGiant exiledCard = new HillGiant();
        harness.setLibrary(player1, List.of(scryCard, exiledCard));

        resolveTriggerAfterChoosingTarget(target, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiledCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(scryCard);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
    }

    private void resolveTriggerAfterChoosingTarget(
            Permanent target, InteractionAnswer.ScryOrder scryOrder) {
        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, scryOrder);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
