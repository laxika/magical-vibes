package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mindleecher.class, Forest.class, GrizzlyBears.class})
class MindleecherTest extends BaseCardTest {

    @Test
    void mutationExilesTopCardOfEachOpponentsLibraryFaceDownAndAllowsPlayingThem() {
        Permanent mindleecher = addCreatureReady(player1, new Mindleecher());
        Card opponentLand = new Forest();
        Card opponentCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentCreature, opponentLand));

        triggerMutation(mindleecher);

        assertThat(gd.getCardsExiledByPermanent(mindleecher.getId()))
                .containsExactly(opponentCreature);
        assertThat(gd.exiledCards).filteredOn(entry -> mindleecher.getId().equals(entry.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentCreature);
    }

    @Test
    void playPermissionRemainsAfterMindleecherLeaves() {
        Permanent mindleecher = addCreatureReady(player1, new Mindleecher());
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiled));

        triggerMutation(mindleecher);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mindleecher);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiled);
    }

    @Test
    void canPlayExiledLandAfterSourceLeavesAndMustRespectLandLimit() {
        Permanent mindleecher = addCreatureReady(player1, new Mindleecher());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        triggerMutation(mindleecher);
        triggerMutation(mindleecher);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mindleecher);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == first);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    void emptyOpponentLibraryDoesNotExileControllersCards() {
        Permanent mindleecher = addCreatureReady(player1, new Mindleecher());
        Card ownCard = new Forest();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of());

        triggerMutation(mindleecher);

        assertThat(gd.getCardsExiledByPermanent(mindleecher.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
    }

    @Test
    void newControllerCannotCastCardsExiledByPreviousController() {
        Permanent mindleecher = addCreatureReady(player1, new Mindleecher());
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiled));
        triggerMutation(mindleecher);
        gd.playerBattlefields.get(player1.getId()).remove(mindleecher);
        gd.playerBattlefields.get(player2.getId()).add(mindleecher);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void canStillLookAtExiledCardsAfterSourceLeavesWithoutManaToCastThem() throws Exception {
        Permanent mindleecher = addCreatureReady(player1, new Mindleecher());
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiled));
        triggerMutation(mindleecher);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mindleecher);
        harness.publishState();

        var mapper = new JacksonConfig().objectMapper();
        GameStateMessage controllerState = mapper.readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        GameStateMessage opponentState = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);

        assertThat(controllerState.lookedAtExileCards()).extracting(card -> card.id())
                .contains(exiled.getId());
        assertThat(opponentState.lookedAtExileCards()).extracting(card -> card.id())
                .doesNotContain(exiled.getId());
    }

    @Test
    void permissionDoesNotAllowCreatureToBeCastDuringOpponentsTurn() {
        Permanent mindleecher = addCreatureReady(player1, new Mindleecher());
        Card exiled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiled));
        triggerMutation(mindleecher);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    private void triggerMutation(Permanent mindleecher) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, mindleecher, List.of(mindleecher.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
