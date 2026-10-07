package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheTemporalAnchor.class, GrizzlyBears.class, ZhalfirinVoid.class, Disenchant.class, Island.class})
class TheTemporalAnchorTest extends BaseCardTest {

    @Test
    @DisplayName("The upkeep ability starts a scry 2 interaction")
    void upkeepAbilityScriesTwo() {
        harness.addToBattlefield(player1, new TheTemporalAnchor());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Cards put on the bottom while scrying are exiled with the Anchor")
    void bottomedScryedCardsAreExiledWithAnchor() {
        Permanent anchor = harness.addToBattlefieldAndReturn(player1, new TheTemporalAnchor());
        Card scryedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(scryedCard, new GrizzlyBears()));
        harness.setHand(player1, List.of(new ZhalfirinVoid()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(anchor.getId())).containsExactly(scryedCard);
    }

    @Test
    @DisplayName("The Anchor's controller may cast a card exiled with it during their turn")
    void controllerMayCastExiledCardDuringTheirTurn() {
        Permanent anchor = harness.addToBattlefieldAndReturn(player1, new TheTemporalAnchor());
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard, new GrizzlyBears()));
        harness.setHand(player1, List.of(new ZhalfirinVoid()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(anchor.getId())).isEmpty();
    }

    @Test
    void keepingEveryCardOnTopDoesNotTriggerExile() {
        harness.addToBattlefield(player1, new TheTemporalAnchor());
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second, new Island()));
        harness.setHand(player1, List.of(new Disenchant()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(first, second);
    }

    @Test
    void exileTriggerStillResolvesAfterAnchorIsDestroyed() {
        Permanent anchor = harness.addToBattlefieldAndReturn(player1, new TheTemporalAnchor());
        Card bottomed = new Island();
        Card kept = new Island();
        harness.setLibrary(player1, List.of(bottomed, kept, new Island()));
        harness.setHand(player1, List.of(new Disenchant()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, anchor.getId());
        harness.assertInGraveyard(player1, "The Temporal Anchor");
        resolveAllTriggers();

        assertThat(gd.findExiledCard(bottomed.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bottomed);
    }

    @Test
    void mayPlayAnExiledLandDuringMainPhase() {
        Permanent anchor = harness.addToBattlefieldAndReturn(player1, new TheTemporalAnchor());
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second, new Island()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        resolveAllTriggers();
        assertThat(gd.getCardsExiledByPermanent(anchor.getId()))
                .containsExactlyInAnyOrder(first, second);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, first.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.getCardsExiledByPermanent(anchor.getId())).containsExactly(second);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    void exiledInstantCannotBeCastDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new TheTemporalAnchor());
        Card exiled = new Disenchant();
        harness.setLibrary(player1, List.of(exiled, new Island(), new Island()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        resolveAllTriggers();
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheTemporalAnchor());

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
        harness.assertOnBattlefield(player2, "The Temporal Anchor");
    }
}
