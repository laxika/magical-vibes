package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CavernWhisperer;
import com.github.laxika.magicalvibes.cards.d.DurableCoilbug;
import com.github.laxika.magicalvibes.cards.h.HeartlessAct;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SonorousHowlbonder.class, CavernWhisperer.class, DurableCoilbug.class, HeartlessAct.class})
class SonorousHowlbonderTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent creature = addReadyCreature(player, card);
        creature.setAttacking(true);
        return creature;
    }

    private void advanceToBlockers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    @Test
    void sonorousHowlbonderNeedsThreeBlockersItself() {
        addReadyAttacker(player1, new SonorousHowlbonder());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());

        advanceToBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    void grantsThreeBlockerRequirementToYourOtherMenaceCreatures() {
        addReadyCreature(player1, new SonorousHowlbonder());
        addReadyAttacker(player1, new CavernWhisperer());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());

        advanceToBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    void doesNotAffectYourCreaturesWithoutMenace() {
        addReadyCreature(player1, new SonorousHowlbonder());
        addReadyAttacker(player1, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());

        advanceToBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    void doesNotAffectOpponentMenaceCreatures() {
        addReadyCreature(player1, new SonorousHowlbonder());
        addReadyCreature(player1, new DurableCoilbug());
        addReadyCreature(player1, new DurableCoilbug());
        addReadyAttacker(player2, new CavernWhisperer());

        advanceToBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
    }

    @Test
    void canBeBlockedByExactlyThreeCreatures() {
        addReadyAttacker(player1, new SonorousHowlbonder());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());

        advanceToBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));
    }

    @Test
    void canBeBlockedByMoreThanThreeCreatures() {
        addReadyAttacker(player1, new SonorousHowlbonder());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());

        advanceToBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0),
                new BlockerAssignment(3, 0)));
    }

    @Test
    void mayRemainUnblockedEvenWhenThreeBlockersAreAvailable() {
        addReadyAttacker(player1, new SonorousHowlbonder());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());

        advanceToBlockers(player1);

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    void multipleHowlbondersDoNotIncreaseTheRequirementBeyondThree() {
        addReadyCreature(player1, new SonorousHowlbonder());
        addReadyAttacker(player1, new SonorousHowlbonder());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());

        advanceToBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1),
                new BlockerAssignment(2, 1)));
    }

    @Test
    void appliesToMenaceGrantedByACounter() {
        addReadyCreature(player1, new SonorousHowlbonder());
        Permanent attacker = addReadyAttacker(player1, new DurableCoilbug());
        attacker.setCounterCount(CounterType.MENACE, 1);
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());

        advanceToBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    void stopsApplyingWhenTheMenaceCounterIsRemoved() {
        addReadyCreature(player1, new SonorousHowlbonder());
        Permanent attacker = addReadyAttacker(player1, new DurableCoilbug());
        attacker.setCounterCount(CounterType.MENACE, 1);
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());

        advanceToBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");

        attacker.setCounterCount(CounterType.MENACE, 0);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    void otherMenaceCreaturesNeedOnlyTwoBlockersAfterHowlbonderDies() {
        Permanent source = addReadyCreature(player1, new SonorousHowlbonder());
        Permanent attacker = addReadyCreature(player1, new CavernWhisperer());
        addReadyCreature(player2, new DurableCoilbug());
        addReadyCreature(player2, new DurableCoilbug());

        harness.setHand(player2, List.of(new HeartlessAct()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castModalInstant(player2, 0, 0, List.of(source.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Sonorous Howlbonder");

        attacker.setAttacking(true);
        advanceToBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
    }
}
