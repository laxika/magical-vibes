package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameBlitz.class, ChandraNalaar.class, GrizzlyBears.class})
class FlameBlitzTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, deals 5 damage to each planeswalker")
    void damagesEachPlaneswalkerAtControllerEndStep() {
        Permanent ownPlaneswalker = addPlaneswalker(player1, 6);
        Permanent opposingPlaneswalker = addPlaneswalker(player2, 6);
        harness.addToBattlefield(player1, new FlameBlitz());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards Flame Blitz and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new FlameBlitz()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flame Blitz");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
