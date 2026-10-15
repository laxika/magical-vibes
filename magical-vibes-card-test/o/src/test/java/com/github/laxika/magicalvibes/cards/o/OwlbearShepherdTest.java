package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OwlbearShepherd.class, GrizzlyBears.class, Forest.class})
class OwlbearShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card at the end step when controlled creatures have total power eight")
    void drawsAtExactTotalPowerThreshold() {
        Permanent owlbear = addCreatureReady(player1, new OwlbearShepherd());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        owlbear.setPowerModifier(1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw a card at the end step below total power eight")
    void doesNotDrawBelowTotalPowerThreshold() {
        addCreatureReady(player1, new OwlbearShepherd());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rechecks total power when the end-step trigger resolves")
    void doesNotDrawWhenPowerFallsBelowThresholdBeforeResolution() {
        Permanent owlbear = addCreatureReady(player1, new OwlbearShepherd());
        owlbear.setPowerModifier(7);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        owlbear.setPowerModifier(6);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger on the opponent's end step even with enough power")
    void doesNotTriggerOnOpponentsEndStep() {
        Permanent owlbear = addCreatureReady(player1, new OwlbearShepherd());
        owlbear.setPowerModifier(7);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not count the opponent's creatures toward total power")
    void doesNotCountOpponentsCreatures() {
        addCreatureReady(player1, new OwlbearShepherd());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Negative creature power reduces the total used for the threshold")
    void includesNegativePowerInTotal() {
        Permanent owlbear = addCreatureReady(player1, new OwlbearShepherd());
        owlbear.setPowerModifier(7);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setPowerModifier(-3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger still draws after Shepherd leaves if remaining power is enough")
    void drawsAfterSourceLeavesBattlefield() {
        Permanent owlbear = addCreatureReady(player1, new OwlbearShepherd());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(owlbear);
        gd.playerGraveyards.get(player1.getId()).add(owlbear.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
