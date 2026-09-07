package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LunarConvocation.class, LifeBurst.class, Forest.class})
class LunarConvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life at your end step if you gained life")
    void eachOpponentLosesLifeAfterLifeGain() {
        harness.addToBattlefield(player1, new LunarConvocation());
        castLifeBurstAtController();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }

    @Test
    @DisplayName("Creates a flying Bat at your end step if you gained and lost life")
    void createsBatAfterGainingAndLosingLife() {
        harness.addToBattlefield(player1, new LunarConvocation());
        harness.setLibrary(player1, List.of(new Forest()));
        castLifeBurstAtController();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInHand(player1, "Forest");

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Bat when you only lost life")
    void doesNotCreateBatWithoutLifeGain() {
        harness.addToBattlefield(player1, new LunarConvocation());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 8);
        harness.assertInHand(player1, "Forest");

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }

    private void castLifeBurstAtController() {
        harness.setHand(player1, List.of(new LifeBurst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
