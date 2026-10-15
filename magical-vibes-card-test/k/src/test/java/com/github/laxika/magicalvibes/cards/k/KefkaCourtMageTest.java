package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KefkaCourtMage.class, KefkaRulerOfRuin.class, GrizzlyBears.class, Shock.class, IronGiant.class})
class KefkaCourtMageTest extends BaseCardTest {

    @Test
    void enterTriggerDrawsForEachDistinctDiscardedCardType() {
        Shock controllerDiscard = new Shock();
        GrizzlyBears opponentDiscard = new GrizzlyBears();
        harness.setHand(player1, List.of(new KefkaCourtMage(), controllerDiscard));
        harness.setHand(player2, List.of(opponentDiscard));
        GrizzlyBears firstDraw = new GrizzlyBears();
        Shock secondDraw = new Shock();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addKefkaMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controllerDiscard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentDiscard);
    }

    @Test
    void enterTriggerCountsARepeatedCardTypeOnlyOnce() {
        Shock controllerDiscard = new Shock();
        Shock opponentDiscard = new Shock();
        harness.setHand(player1, List.of(new KefkaCourtMage(), controllerDiscard));
        harness.setHand(player2, List.of(opponentDiscard));
        Shock draw = new Shock();
        harness.setLibrary(player1, List.of(draw, new GrizzlyBears()));
        addKefkaMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void attackTriggerAlsoCollectsDiscardsAndDraws() {
        addCreatureReady(player1, new KefkaCourtMage());
        Shock controllerDiscard = new Shock();
        GrizzlyBears opponentDiscard = new GrizzlyBears();
        harness.setHand(player1, List.of(controllerDiscard));
        harness.setHand(player2, List.of(opponentDiscard));
        harness.setLibrary(player1, List.of(new Shock(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void activatedAbilitySacrificesAnOpponentPermanentThenTransforms() {
        Permanent kefka = addCreatureReady(player1, new KefkaCourtMage());
        Permanent opponentPermanent = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentPermanent.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentPermanent);
        assertThat(kefka.isTransformed()).isTrue();
    }

    @Test
    void backFaceDrawsLifeLostByAnOpponentDuringItsControllersTurn() {
        KefkaCourtMage front = new KefkaCourtMage();
        Permanent kefka = new Permanent(front);
        kefka.setCard(front.getBackFaceCard());
        kefka.setTransformed(true);
        kefka.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kefka);
        harness.setHand(player1, List.of(new Shock()));
        Shock firstDraw = new Shock();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void discardChoicesAreCompletedBeforeEitherCardIsDiscarded() {
        addCreatureReady(player1, new KefkaCourtMage());
        KefkaCourtMage controllerDiscard = new KefkaCourtMage();
        IronGiant opponentDiscard = new IronGiant();
        harness.setHand(player1, List.of(controllerDiscard));
        harness.setHand(player2, List.of(opponentDiscard));
        harness.setLibrary(player1, List.of(new KefkaCourtMage(), new IronGiant()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(controllerDiscard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentDiscard);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controllerDiscard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentDiscard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void artifactCreatureDiscardCountsBothTypesWhenControllerHasNoCards() {
        addCreatureReady(player1, new KefkaCourtMage());
        IronGiant discarded = new IronGiant();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(discarded));
        KefkaCourtMage firstDraw = new KefkaCourtMage();
        IronGiant secondDraw = new IronGiant();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new IronGiant()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void emptyHandsCauseNoDraws() {
        addCreatureReady(player1, new KefkaCourtMage());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        IronGiant topCard = new IronGiant();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void transformsEvenWhenOpponentHasNoPermanents() {
        Permanent kefka = addCreatureReady(player1, new KefkaCourtMage());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(kefka.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kefka);
    }

    @Test
    void activationIsRejectedOutsideMainPhase() {
        addCreatureReady(player1, new KefkaCourtMage());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activationIsRejectedDuringOpponentsMainPhase() {
        addCreatureReady(player1, new KefkaCourtMage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activationIsRejectedWhileAnotherSpellIsOnTheStack() {
        addCreatureReady(player1, new KefkaCourtMage());
        prepareMainPhase();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void backFaceDrawsForEveryLifeLossEventDuringControllersTurn() {
        harness.addToBattlefield(player1, new KefkaRulerOfRuin());
        prepareMainPhase();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        IronGiant firstDraw = new IronGiant();
        KefkaCourtMage secondDraw = new KefkaCourtMage();
        IronGiant thirdDraw = new IronGiant();
        KefkaCourtMage fourthDraw = new KefkaCourtMage();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw, fourthDraw));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstDraw, secondDraw, thirdDraw, fourthDraw);
    }

    @Test
    void backFaceDoesNotDrawOnOpponentsTurn() {
        harness.addToBattlefield(player1, new KefkaRulerOfRuin());
        harness.setHand(player1, List.of(new Shock()));
        IronGiant topCard = new IronGiant();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void backFaceDoesNotDrawForItsControllersLifeLoss() {
        harness.addToBattlefield(player1, new KefkaRulerOfRuin());
        harness.setHand(player1, List.of(new Shock()));
        IronGiant topCard = new IronGiant();
        harness.setLibrary(player1, List.of(topCard));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private void addKefkaMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
