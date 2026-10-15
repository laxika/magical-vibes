package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LurkingLizards;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheDeathOfGwenStacy.class, LurkingLizards.class, ObstinateBaloth.class})
class TheDeathOfGwenStacyTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I destroys a target creature")
    void chapterIDestroysTargetCreature() {
        Permanent target = addCreatureReady(player2, new LurkingLizards());
        addSaga(player1, 0);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Chapter II lets each player discard or lose 3 life")
    void chapterIIMakesEachPlayerChooseDiscardOrLifeLoss() {
        addSaga(player1, 1);
        harness.setHand(player1, List.of(new LurkingLizards()));
        harness.setHand(player2, List.of(new LurkingLizards()));

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter III exiles any number of target players' graveyards")
    void chapterIIIExilesSelectedPlayersGraveyards() {
        addSaga(player1, 2);
        harness.setGraveyard(player1, List.of(new LurkingLizards()));
        harness.setGraveyard(player2, List.of(new LurkingLizards(), new LurkingLizards()));

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).isEmpty();
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof LurkingLizards)
                .isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Chapter II keeps chosen cards hidden until all players choose")
    void chapterIIDiscardsSimultaneously() {
        addSaga(player1, 1);
        LurkingLizards first = new LurkingLizards();
        LurkingLizards second = new LurkingLizards();
        harness.setHand(player1, List.of(first));
        harness.setHand(player2, List.of(second));

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first);
        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(second);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Chapter II waits for discards before penalizing a player with no cards")
    void chapterIIEmptyHandLifeLossWaitsForDiscards() {
        addSaga(player1, 1);
        harness.setHand(player1, List.of(new LurkingLizards()));
        harness.setHand(player2, List.of());

        triggerChapter();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Chapter II makes both empty-handed players lose life")
    void chapterIIBothEmptyHandsLoseLife() {
        addSaga(player1, 1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        triggerChapter();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({TheDeathOfGwenStacy.class, LurkingLizards.class, ObstinateBaloth.class})
    @DisplayName("Chapter II preserves the opponent-controlled discard source")
    void chapterIIHonorsOpponentDiscardReplacement() {
        addSaga(player1, 1);
        harness.setHand(player1, List.of(new LurkingLizards()));
        harness.setHand(player2, List.of(new ObstinateBaloth()));

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Obstinate Baloth");
        harness.assertNotInGraveyard(player2, "Obstinate Baloth");
        harness.assertLife(player2, 24);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Chapter III can choose no players and still sacrifices the Saga")
    void chapterIIICanChooseNoPlayers() {
        Permanent saga = addSaga(player1, 2);
        LurkingLizards first = new LurkingLizards();
        LurkingLizards second = new LurkingLizards();
        harness.setGraveyard(player1, List.of(first));
        harness.setGraveyard(player2, List.of(second));

        triggerChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, saga.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    @DisplayName("Chapter III exiles only the chosen player's graveyard")
    void chapterIIILeavesUnselectedGraveyardAlone() {
        Permanent saga = addSaga(player1, 2);
        LurkingLizards first = new LurkingLizards();
        LurkingLizards second = new LurkingLizards();
        harness.setGraveyard(player1, List.of(first));
        harness.setGraveyard(player2, List.of(second));

        triggerChapter();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, saga.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    @DisplayName("Entering the battlefield triggers chapter I")
    void enteringTriggersChapterI() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LurkingLizards());
        harness.castFromHand(player1, new TheDeathOfGwenStacy(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Lurking Lizards");
        harness.assertOnBattlefield(player1, "The Death of Gwen Stacy");
    }

    @Test
    @DisplayName("Chapter I may destroy the controller's creature but cannot target the Saga")
    void chapterICanTargetOwnCreatureOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LurkingLizards());
        Permanent saga = addSaga(player1, 0);

        triggerChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).contains(target.getId()).doesNotContain(saga.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target).contains(saga);
        harness.assertInGraveyard(player1, "Lurking Lizards");
    }

    private Permanent addSaga(com.github.laxika.magicalvibes.model.Player player, int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player, new TheDeathOfGwenStacy());
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
