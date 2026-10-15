package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExitSpecialist.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class ExitSpecialistTest extends BaseCardTest {

    @Test
    void turningFaceUpReturnsAnotherTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExitSpecialist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent specialist = findPermanent(player1, "Exit Specialist");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(specialist));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(specialist.isFaceDown()).isFalse();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void disguiseWardCountersOpponentSpellWhileFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ExitSpecialist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent specialist = findPermanent(player1, "Exit Specialist");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, specialist.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(specialist.isFaceDown()).isTrue();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void cannotBeBlockedByCreatureWithPowerThreeOrGreater() {
        addCreatureReady(player2, new HillGiant());

        Permanent attacker = addCreatureReady(player1, new ExitSpecialist());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Exit Specialist");
    }

    @Test
    void canBeBlockedByCreatureWithPowerTwo() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent attacker = addCreatureReady(player1, new ExitSpecialist());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void disguiseCreatesOnlyOneWardTrigger() {
        Permanent specialist = castFaceDownSpecialist();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, specialist.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void faceDownSpecialistCanBeBlockedByPowerThreeCreature() {
        Permanent specialist = castFaceDownSpecialist();
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        specialist.setSummoningSick(false);
        specialist.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void turningFaceUpCanReturnAnotherCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent specialist = castFaceDownSpecialist();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(specialist));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Exit Specialist");
    }

    @Test
    void turningFaceUpWithoutAnotherCreatureDoesNotReturnItself() {
        Permanent specialist = castFaceDownSpecialist();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, 0);
        resolveAllTriggers();

        assertThat(specialist.isFaceDown()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Exit Specialist");
        harness.assertNotInHand(player1, "Exit Specialist");
    }

    @Test
    void faceUpSpecialistDoesNotHaveDisguiseWard() {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new ExitSpecialist());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, specialist.getId());

        harness.assertInGraveyard(player1, "Exit Specialist");
        harness.assertNotOnBattlefield(player1, "Exit Specialist");
    }

    private Permanent castFaceDownSpecialist() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ExitSpecialist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Exit Specialist");
    }
}
