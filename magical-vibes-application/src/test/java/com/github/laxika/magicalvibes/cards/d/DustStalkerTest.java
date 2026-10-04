package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DustStalker.class, Memnite.class, GrizzlyBears.class, Forest.class})
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

    @Test
    @DisplayName("Returns during the opponent's end step even when the opponent controls a colorless creature")
    void returnsDuringOpponentsEndStep() {
        addCreatureReady(player1, new DustStalker());
        addCreatureReady(player2, new DustStalker());

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dust Stalker");
        harness.assertNotOnBattlefield(player2, "Dust Stalker");
        harness.assertInHand(player1, "Dust Stalker");
        harness.assertInHand(player2, "Dust Stalker");
    }

    @Test
    @DisplayName("Two Dust Stalkers controlled by the same player keep each other on the battlefield")
    void twoStalkersPreventEachOthersReturn() {
        addCreatureReady(player1, new DustStalker());
        addCreatureReady(player1, new DustStalker());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A colorless noncreature permanent does not prevent the return")
    void colorlessLandDoesNotPreventReturn() {
        addCreatureReady(player1, new DustStalker());
        harness.addToBattlefield(player1, new Forest());

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dust Stalker");
        harness.assertInHand(player1, "Dust Stalker");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("A controlled Dust Stalker returns to its owner's hand")
    void returnsToOwnerRatherThanController() {
        Permanent stalker = addCreatureReady(player2, new DustStalker());
        gd.stolenCreatures.put(stalker.getId(), player1.getId());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dust Stalker");
        assertThat(gd.playerHands.get(player1.getId())).contains(stalker.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(stalker.getCard());
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
