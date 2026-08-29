package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
