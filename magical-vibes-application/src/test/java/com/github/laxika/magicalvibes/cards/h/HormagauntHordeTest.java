package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HormagauntHorde.class, Forest.class, GrizzlyBears.class})
class HormagauntHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous enters with X +1/+1 counters without drawing below X=5")
    void ravenousBelowThreshold() {
        castHormagauntHorde(3);

        assertThat(findPermanent(player1, "Hormagaunt Horde").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous draws a card when X is 5 or more")
    void ravenousDrawsAtThreshold() {
        castHormagauntHorde(5);

        assertThat(findPermanent(player1, "Hormagaunt Horde").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("May pay to return itself from the graveyard when a land enters")
    void mayPayToReturnFromGraveyardOnLandfall() {
        HormagauntHorde horde = new HormagauntHorde();
        harness.setGraveyard(player1, List.of(horde));
        prepareMainPhase();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Hormagaunt Horde");
        harness.assertNotInGraveyard(player1, "Hormagaunt Horde");
    }

    @Test
    @DisplayName("Declining the landfall payment keeps itself in the graveyard")
    void decliningLandfallPaymentKeepsItInGraveyard() {
        HormagauntHorde horde = new HormagauntHorde();
        harness.setGraveyard(player1, List.of(horde));
        prepareMainPhase();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Hormagaunt Horde");
    }

    private void castHormagauntHorde(int x) {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HormagauntHorde()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x + 1);

        gs.playCard(gd, player1, 0, x, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
