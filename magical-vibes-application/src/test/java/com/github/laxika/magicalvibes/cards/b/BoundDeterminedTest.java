package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cytoshape;
import com.github.laxika.magicalvibes.cards.s.SimicSkySwallower;
import com.github.laxika.magicalvibes.cards.s.SimicInitiate;
import com.github.laxika.magicalvibes.cards.v.Voidslime;
import com.github.laxika.magicalvibes.cards.w.WreckingBall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoundDetermined.class, Cytoshape.class, SimicSkySwallower.class, SimicInitiate.class,
        Voidslime.class, WreckingBall.class})
class BoundDeterminedTest extends BaseCardTest {

    @Test
    void boundReturnsUpToTheSacrificedCreaturesColorCountAndExilesItself() {
        Permanent swallower = harness.addToBattlefieldAndReturn(player1, new SimicSkySwallower());
        Card cytoshape = new Cytoshape();
        Card wreckingBall = new WreckingBall();
        Card voidslime = new Voidslime();
        harness.setGraveyard(player1, List.of(cytoshape, wreckingBall, voidslime));

        BoundDetermined bound = new BoundDetermined();
        harness.setHand(player1, List.of(bound));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, swallower.getId());

        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bound.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(cytoshape, wreckingBall)
                .doesNotContain(voidslime);
        harness.assertInGraveyard(player1, "Simic Sky Swallower");
    }

    @Test
    void boundCanReturnFewerCardsThanTheSacrificedCreaturesColorCount() {
        Permanent swallower = harness.addToBattlefieldAndReturn(player1, new SimicSkySwallower());
        Card cytoshape = new Cytoshape();
        Card wreckingBall = new WreckingBall();
        harness.setGraveyard(player1, List.of(cytoshape, wreckingBall));

        BoundDetermined bound = new BoundDetermined();
        harness.setHand(player1, List.of(bound));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, swallower.getId());

        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(cytoshape)
                .doesNotContain(wreckingBall);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wreckingBall);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bound.getId()));
    }

    @Test
    void boundExilesItselfWhenThereIsNoCreatureToSacrifice() {
        BoundDetermined bound = new BoundDetermined();
        harness.setHand(player1, List.of(bound));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bound.getId()));
        harness.assertNotInGraveyard(player1, "Bound // Determined");
    }

    @Test
    void determinedDrawsAndMakesLaterSpellsUncounterable() {
        Cytoshape drawn = new Cytoshape();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new BoundDetermined()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);

        SimicInitiate initiate = new SimicInitiate();
        harness.setHand(player1, List.of(initiate));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Voidslime()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, initiate.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Simic Initiate");
    }

    @Test
    void boundCanReturnTheCreatureItJustSacrificed() {
        SimicInitiate initiate = new SimicInitiate();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, initiate);
        BoundDetermined bound = new BoundDetermined();
        harness.setHand(player1, List.of(bound));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(initiate);
        harness.assertNotOnBattlefield(player1, "Simic Initiate");
        harness.assertNotInGraveyard(player1, "Simic Initiate");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bound);
    }

    @Test
    void boundCanReturnZeroCardsAfterSacrificingAMulticoloredCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SimicSkySwallower());
        Cytoshape card = new Cytoshape();
        harness.setGraveyard(player1, List.of(card));
        BoundDetermined bound = new BoundDetermined();
        harness.setHand(player1, List.of(bound));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        harness.assertInGraveyard(player1, "Simic Sky Swallower");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bound);
    }

    @Test
    void determinedCanBeCounteredBeforeItResolves() {
        BoundDetermined determined = new BoundDetermined();
        Cytoshape card = new Cytoshape();
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(determined));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Voidslime()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, determined.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(determined);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(determined);
    }

    @Test
    void determinedProtectsASpellAlreadyOnTheStack() {
        SimicInitiate initiate = new SimicInitiate();
        BoundDetermined determined = new BoundDetermined();
        Cytoshape drawn = new Cytoshape();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(initiate, determined));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Voidslime()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, initiate.getId());
        harness.passPriority(player2);
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(determined);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(determined);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Simic Initiate");
        harness.assertInGraveyard(player2, "Voidslime");
    }
}
