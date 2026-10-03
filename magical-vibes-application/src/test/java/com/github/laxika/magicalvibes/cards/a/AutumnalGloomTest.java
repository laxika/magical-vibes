package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.s.StrionicResonator;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AutumnalGloom.class, AncientOfTheEquinox.class, DevilthornFox.class,
        Forest.class, DualShot.class, MagnifyingGlass.class})
class AutumnalGloomTest extends BaseCardTest {

    @Test
    void activatedAbilityMillsOneCard() {
        harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());
        Card milledCard = new DevilthornFox();
        harness.setLibrary(player1, List.of(milledCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledCard);
    }

    @Test
    void transformsAtEndStepWithDelirium() {
        harness.setGraveyard(player1, List.of(new DevilthornFox(), new Forest(), new DualShot(), new MagnifyingGlass()));
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gloom.isTransformed()).isTrue();
    }

    @Test
    void doesNotTransformAtEndStepWithoutDelirium() {
        harness.setGraveyard(player1, List.of(new DevilthornFox(), new Forest(), new DualShot()));
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());

        advanceToEndStep();

        assertThat(gloom.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canMillRepeatedlyWithoutTapping() {
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());
        Card first = new DevilthornFox();
        Card second = new Forest();
        Card third = new DualShot();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gloom.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void millingEmptyLibraryDoesNotLoseTheGame() {
        harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void deliriumIsCheckedAgainWhenTriggerResolves() {
        harness.setGraveyard(player1, List.of(new DevilthornFox(), new Forest(), new DualShot(), new MagnifyingGlass()));
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new DevilthornFox(), new Forest(), new DualShot()));
        harness.passBothPriorities();

        assertThat(gloom.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainingDeliriumAfterEndStepBeginsDoesNotTriggerTransformation() {
        harness.setGraveyard(player1, List.of(new DevilthornFox(), new Forest(), new DualShot()));
        harness.setLibrary(player1, List.of(new MagnifyingGlass()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());

        advanceToEndStep();
        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gloom.isTransformed()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTransformDuringOpponentsEndStep() {
        harness.setGraveyard(player1, List.of(new DevilthornFox(), new Forest(), new DualShot(), new MagnifyingGlass()));
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gloom.isTransformed()).isFalse();
    }

    @Test
    void opponentsGraveyardDoesNotEnableDelirium() {
        harness.setGraveyard(player2, List.of(new DevilthornFox(), new Forest(), new DualShot(), new MagnifyingGlass()));
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gloom.isTransformed()).isFalse();
    }

    @Test
    void fourCardsWithOnlyThreeTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(new DevilthornFox(), new DevilthornFox(), new Forest(), new DualShot()));
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gloom.isTransformed()).isFalse();
    }

    @Test
    void transformedFaceCannotBeTargetedByOpponent() {
        Permanent ancient = transformGloom();
        harness.setHand(player2, List.of(new DualShot()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, ancient.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void transformedFaceCanBeTargetedByController() {
        Permanent ancient = transformGloom();
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ancient.getId());

        assertThat(ancient.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ancient of the Equinox");
    }

    @Test
    void transformedFaceDealsExcessCombatDamageToDefendingPlayer() {
        Permanent ancient = transformGloom();
        ancient.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new DevilthornFox());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 1, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Devilthorn Fox");
        harness.assertOnBattlefield(player1, "Ancient of the Equinox");
    }

    @Test
    void transformedFaceDoesNotTransformBackAtNextEndStep() {
        Permanent ancient = transformGloom();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(ancient.isTransformed()).isTrue();
    }

    @Test
    @CardUsed({StrionicResonator.class})
    void copiedEndStepTriggerDoesNotTransformAncientBack() {
        harness.setGraveyard(player1, List.of(new DevilthornFox(), new Forest(), new DualShot(), new MagnifyingGlass()));
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());
        harness.addToBattlefield(player1, new StrionicResonator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 1, null, gd.stack.getLast().getTargetableId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gloom.isTransformed()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gloom.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Ancient of the Equinox");
    }

    private Permanent transformGloom() {
        harness.setGraveyard(player1, List.of(new DevilthornFox(), new Forest(), new DualShot(), new MagnifyingGlass()));
        Permanent gloom = harness.addToBattlefieldAndReturn(player1, new AutumnalGloom());
        advanceToEndStep();
        harness.passBothPriorities();
        assertThat(gloom.isTransformed()).isTrue();
        return gloom;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
