package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
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

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(target.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        harness.passBothPriorities();

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

    @Test
    void stillScriesAndExilesWhenNoCreaturesRemain() {
        harness.addToBattlefield(player1, new CaitSithFortuneTeller());
        HillGiant topCard = new HillGiant();
        harness.setLibrary(player1, List.of(topCard));

        advanceToBeginningOfCombat(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotCreateABoostTrigger() {
        Permanent cait = harness.addToBattlefieldAndReturn(player1, new CaitSithFortuneTeller());
        harness.setLibrary(player1, List.of());

        advanceToBeginningOfCombat(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, cait)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new CaitSithFortuneTeller());
        HillGiant topCard = new HillGiant();
        harness.setLibrary(player1, List.of(topCard));

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void exiledCreatureCanBeCastForItsNormalCostDuringMainPhase() {
        Permanent cait = harness.addToBattlefieldAndReturn(player1, new CaitSithFortuneTeller());
        HillGiant topCard = new HillGiant();
        harness.setLibrary(player1, List.of(topCard));
        resolveTriggerAfterChoosingTarget(cait,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void exilingALandGrantsPermissionWithoutIncreasingPower() {
        Permanent cait = harness.addToBattlefieldAndReturn(player1, new CaitSithFortuneTeller());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        resolveTriggerAfterChoosingTarget(cait,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gqs.getEffectivePower(gd, cait)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cait)).isEqualTo(3);
    }

    private void resolveTriggerAfterChoosingTarget(
            Permanent target, InteractionAnswer.ScryOrder scryOrder) {
        advanceToBeginningOfCombat(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, scryOrder);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
