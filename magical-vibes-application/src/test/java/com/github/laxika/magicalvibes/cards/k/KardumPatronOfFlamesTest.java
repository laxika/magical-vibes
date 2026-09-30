package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KardumPatronOfFlames.class, GrizzlyBears.class, LlanowarElves.class, Murder.class})
class KardumPatronOfFlamesTest extends BaseCardTest {

    @Test
    void attackAddsFlameCounterAndSeeksExactManaValueFaceDownWithSource() {
        Permanent kardum = addCreatureReady(player1, new KardumPatronOfFlames());
        Card sought = new LlanowarElves();
        Card wrongManaValue = new Murder();
        harness.setLibrary(player1, List.of(sought, wrongManaValue));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(kardum.getCounterCount(CounterType.FLAME)).isEqualTo(1);
        assertThat(gd.getCardsExiledByPermanent(kardum.getId())).singleElement().satisfies(card ->
                assertThat(card).isSameAs(sought));
        assertThat(gd.exiledCards)
                .filteredOn(exiled -> kardum.getId().equals(exiled.sourcePermanentId()))
                .singleElement()
                .satisfies(exiled -> assertThat(exiled.faceDown()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wrongManaValue);
    }

    @Test
    void deathReturnsOwnedCardsAndDiscardsThoseCardsAtNextTurnEndStep() {
        Permanent kardum = addCreatureReady(player1, new KardumPatronOfFlames());
        Card sought = new LlanowarElves();
        Card unrelated = new GrizzlyBears();
        Card murder = new Murder();
        harness.setLibrary(player1, List.of(sought));
        harness.setHand(player1, List.of(unrelated, murder));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.getCardsExiledByPermanent(kardum.getId())).containsExactly(sought);

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 3);
        harness.castInstant(player1, 1, kardum.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(unrelated, sought);
        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(unrelated);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sought);
    }
}
