package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheEndstone.class, Forest.class, Opt.class})
class TheEndstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell draws a card")
    void castingSpellDraws() {
        harness.addToBattlefield(player1, new TheEndstone());
        harness.setHand(player1, new ArrayList<>(List.of(new Opt())));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Playing a land draws a card")
    void playingLandDraws() {
        harness.addToBattlefield(player1, new TheEndstone());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Controller's end step sets their life total to half their starting life total")
    void endStepSetsLifeToHalfStartingTotal() {
        harness.addToBattlefield(player1, new TheEndstone());
        harness.setLife(player1, 17);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    void endStepGainsLifeWhenBelowHalfStartingTotal() {
        harness.addToBattlefield(player1, new TheEndstone());
        harness.setLife(player1, 3);
        harness.setLife(player2, 17);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void commanderEndStepUsesFortyStartingLife() {
        gd.format = DeckFormat.COMMANDER;
        harness.addToBattlefield(player1, new TheEndstone());
        harness.setLife(player1, 37);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void opponentsEndStepDoesNotChangeLife() {
        harness.addToBattlefield(player1, new TheEndstone());
        harness.setLife(player1, 17);
        harness.setLife(player2, 13);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void opponentsLandDoesNotDraw() {
        harness.addToBattlefield(player1, new TheEndstone());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    void opponentsSpellDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new TheEndstone());
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    void castingEndstoneDoesNotTriggerItself() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TheEndstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(countPermanents(player1, "The Endstone")).isEqualTo(1);
    }
}
