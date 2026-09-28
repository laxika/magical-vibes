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

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
