package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vault112SadisticSimulation.class, GrizzlyBears.class, Shock.class, Mountain.class})
class Vault112SadisticSimulationTest extends BaseCardTest {

    @Test
    void chaptersIAndIITapStunAndGrantEnergy() {
        Permanent saga = addSaga(0);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    void chapterIIIPaysEnergyExilesThatManyAndOffersOneForFree() {
        addSaga(2);
        Card first = new Shock();
        Card second = new Mountain();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        gd.playerEnergyCounters.put(player1.getId(), 2);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        PendingInteraction.ExiledCardMayPlayChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).hasSize(2);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        UUID chosenId = choice.validCardIds().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosenId));

        assertThat(gd.exilePlayPermissions.get(chosenId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(chosenId);
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(chosenId);
    }

    @Test
    void chapterIIIPayingZeroDoesNotShuffleOrExile() {
        addSaga(2);
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        gd.playerEnergyCounters.put(player1.getId(), 1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void chapterIIIMayDeclineToPlayAnExiledCard() {
        addSaga(2);
        Card first = new Shock();
        Card second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        gd.playerEnergyCounters.put(player1.getId(), 1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        PendingInteraction.ExiledCardMayPlayChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(choice.allowNoChoice()).isTrue();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault112SadisticSimulation());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
