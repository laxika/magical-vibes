package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DustStalker.class, Memnite.class, GrizzlyBears.class})
class DustStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand at the end step without another colorless creature")
    void returnsItselfWithoutAnotherColorlessCreature() {
        Permanent stalker = addCreatureReady(player1, new DustStalker());

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dust Stalker");
        assertThat(gd.playerHands.get(player1.getId())).contains(stalker.getCard());
    }

    @Test
    @DisplayName("Does not return itself when its controller has another colorless creature")
    void doesNotReturnWithAnotherColorlessCreature() {
        addCreatureReady(player1, new DustStalker());
        addCreatureReady(player1, new Memnite());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dust Stalker");
    }

    @Test
    @DisplayName("A colored creature does not prevent the return")
    void coloredCreatureDoesNotPreventReturn() {
        addCreatureReady(player1, new DustStalker());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dust Stalker");
    }

    @Test
    @DisplayName("The condition is checked again when the trigger resolves")
    void conditionRecheckedAtResolution() {
        addCreatureReady(player1, new DustStalker());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Memnite());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dust Stalker");
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
