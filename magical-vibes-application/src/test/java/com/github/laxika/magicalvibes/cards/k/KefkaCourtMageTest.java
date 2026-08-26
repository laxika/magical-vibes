package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KefkaCourtMage.class, KefkaRulerOfRuin.class, GrizzlyBears.class, Shock.class})
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
        harness.handlePermanentChosen(player2, opponentPermanent.getId());

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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
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
