package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BlasterHulk;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vault112SadisticSimulation.class, GrizzlyBears.class, Shock.class, Mountain.class, BlasterHulk.class})
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
        Card first = new Mountain();
        Card second = new Mountain();
        Card third = new Mountain();
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(chosenId));
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(chosenId);
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

    @Test
    void chaptersGrantEnergyWhenNoCreaturesExist() {
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);

        triggerChapter();
        harness.passBothPriorities();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void chapterICanChooseNoTargetEvenWhenACreatureExists() {
        addSaga(0);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void chapterIDoesNotGrantEnergyWhenItsOnlyTargetLeaves() {
        addSaga(0);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, bear.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void chapterIIICastsChosenCreatureDuringResolutionWithoutMana() {
        addSaga(2);
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        gd.playerEnergyCounters.put(player1.getId(), 1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(creature.getId());

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Vault 112: Sadistic Simulation");
    }

    @Test
    void chapterIIIWithNoEnergyDoesNotExileOrOfferAPlay() {
        addSaga(2);
        Card top = new Mountain();
        harness.setLibrary(player1, List.of(top));

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Vault 112: Sadistic Simulation");
    }

    @Test
    void chapterIIICanPayMoreEnergyThanCardsRemaining() {
        addSaga(2);
        Card top = new Mountain();
        harness.setLibrary(player1, List.of(top));
        gd.playerEnergyCounters.put(player1.getId(), 4);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 4);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
    }

    @Test
    void energyPaidForChapterIIIReducesBlasterHulksCost() {
        addSaga(2);
        harness.setLibrary(player1, List.of(new Mountain()));
        gd.playerEnergyCounters.put(player1.getId(), 4);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 4);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.setHand(player1, List.of(new BlasterHulk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blaster Hulk");
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
