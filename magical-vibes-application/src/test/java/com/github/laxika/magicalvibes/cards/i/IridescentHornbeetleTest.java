package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({IridescentHornbeetle.class, IronshellBeetle.class, GrizzlyBears.class})
class IridescentHornbeetleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Insect token for each +1/+1 counter put on your creatures")
    void createsTokensForCountersPutOnYourCreatures() {
        addCreatureReady(player1, new IridescentHornbeetle());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(target);
        putCounterOn(target);
        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Insect")).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("Does not count a counter put on an opponent's creature")
    void doesNotCountCounterPutOnOpponentsCreature() {
        addCreatureReady(player1, new IridescentHornbeetle());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        putCounterOn(target);
        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Insect")).filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    @DisplayName("Does not count a counter put by an opponent")
    void doesNotCountCounterPutByOpponent() {
        addCreatureReady(player1, new IridescentHornbeetle());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new IronshellBeetle()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.playCard(gd, player2, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Insect")).filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    private void putCounterOn(Permanent target) {
        harness.setHand(player1, List.of(new IronshellBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
