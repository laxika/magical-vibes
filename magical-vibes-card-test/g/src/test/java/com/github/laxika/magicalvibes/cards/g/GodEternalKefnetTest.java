package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FinaleOfRevelation;
import com.github.laxika.magicalvibes.cards.h.Heartfire;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GodEternalKefnet.class, Divination.class, Forest.class, Mountain.class, Plains.class,
        SwordsToPlowshares.class, WrathOfGod.class, FinaleOfRevelation.class, Heartfire.class})
class GodEternalKefnetTest extends BaseCardTest {

    @Test
    @DisplayName("The first drawn instant or sorcery is copied and may be cast for two less")
    void copiesFirstDrawnInstantOrSorcery() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new Divination(), new Forest(), new Mountain(), new Plains()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Declining to reveal the first drawn instant or sorcery creates no copy")
    void mayDeclineReveal() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new Divination(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The first drawn land may be revealed without creating a copy")
    void doesNotCopyOtherCardTypes() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The death trigger may put Kefnet third from the top")
    void deathTriggerPutsKefnetThirdFromTop() {
        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addToBattlefield(player1, new GodEternalKefnet());
        Card kefnet = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), kefnet.getId(), third.getId());
    }

    @Test
    @DisplayName("The first card drawn on an opponent's turn can be copied, including a sorcery")
    void copiesSorceryDrawnOnOpponentsTurn() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new Divination(), new Forest(), new Mountain(), new Plains()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        harness.assertInHand(player1, "Divination");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The reveal decision is made during the draw before any priority passes")
    void revealChoiceHappensAsCardIsDrawn() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new FinaleOfRevelation(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Revealing a card does not require casting its copy")
    void mayDeclineCastingCopy() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new Divination(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An X spell copy permits choosing a nonzero X and applies the reduction to it")
    void copyAllowsChoosingX() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new FinaleOfRevelation(), new Forest(), new Mountain(), new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        harness.assertInHand(player1, "Finale of Revelation");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("A payable sacrifice cost does not prevent casting a revealed copy")
    void copyWithSacrificeCostOffersCastingChoices() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new Heartfire(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertInHand(player1, "Heartfire");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Drawing a later instant or sorcery after declining the first reveal creates no offer")
    void laterDrawCannotBeRevealed() {
        harness.addToBattlefield(player1, new GodEternalKefnet());
        harness.setLibrary(player1, List.of(new Divination(), new FinaleOfRevelation(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Finale of Revelation");
    }

    @Test
    @DisplayName("The death trigger may be declined")
    void mayLeaveKefnetInGraveyard() {
        harness.setLibrary(player1, List.of(new Forest()));
        var kefnet = harness.addToBattlefieldAndReturn(player1, new GodEternalKefnet());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, kefnet));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "God-Eternal Kefnet");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Kefnet goes on the bottom when fewer than two cards remain in the library")
    void shortLibraryReceivesKefnetOnBottom() {
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        var kefnet = harness.addToBattlefieldAndReturn(player1, new GodEternalKefnet());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, kefnet));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), kefnet.getCard().getId());
        harness.assertNotInGraveyard(player1, "God-Eternal Kefnet");
    }

    @Test
    @DisplayName("An older death trigger cannot move Kefnet after it leaves and reenters the graveyard")
    void oldDeathTriggerDoesNotTrackNewGraveyardObject() {
        harness.setLibrary(player1, List.of(new Forest()));
        Card card = new GodEternalKefnet();
        var first = harness.addToBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, first));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, card.getId()));
        var returned = harness.enterBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, returned));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "God-Eternal Kefnet");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The exile trigger may put Kefnet third from the top")
    void exileTriggerPutsKefnetThirdFromTop() {
        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));
        var kefnetPermanent = harness.addToBattlefieldAndReturn(player1, new GodEternalKefnet());
        Card kefnet = kefnetPermanent.getCard();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, kefnetPermanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), kefnet.getId(), third.getId());
    }
}
