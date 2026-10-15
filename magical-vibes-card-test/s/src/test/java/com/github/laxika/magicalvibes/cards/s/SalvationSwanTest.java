package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SalvationSwan.class, GrizzlyBears.class, SuntailHawk.class, WindDrake.class, Conspiracy.class})
class SalvationSwanTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry exiles a nonflying creature and returns it with a flying counter")
    void ownEntryReturnsTargetWithFlyingCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID originalId = target.getId();

        castSwan();
        resolveTrigger(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Another Bird entry also triggers the ability")
    void anotherBirdEntryTriggers() {
        harness.addToBattlefield(player1, new SalvationSwan());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new SuntailHawk(), "{W}");

        resolveTrigger(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger cannot target a creature with flying")
    void cannotTargetCreatureWithFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent flyingTarget = harness.addToBattlefieldAndReturn(player1, new WindDrake());

        castSwan();
        harness.passBothPriorities();
        assertThatThrownBy(() -> {
            harness.handlePermanentChosen(player1, flyingTarget.getId());
        }).isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    void canChooseNoTargetEvenWhenLegalTargetExists() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSwan();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isEqualTo(target.getId());
        assertThat(target.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    void cannotTargetOpponentsCreature() {
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSwan();
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void nonBirdEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new SalvationSwan());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsBirdEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new SalvationSwan());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void stolenCreatureReturnsToItsOwner() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player2.getId());

        castSwan();
        resolveTrigger(target);
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
    }

    @Test
    void delayedReturnSurvivesSwanLeavingBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castSwan();
        resolveTrigger(target);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Salvation Swan")));

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Salvation Swan");
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.FLYING))
                .isEqualTo(1);
    }

    @Test
    void ownEntryStillTriggersWhenSwanIsNotABird() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSwan();
        resolveTrigger(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        advanceToEndStep();
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.FLYING))
                .isEqualTo(1);
    }

    @Test
    void targetGainingFlyingBeforeResolutionIsNotExiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castSwan();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        target.setCounterCount(CounterType.FLYING, 1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isEqualTo(target.getId());
    }

    @Test
    void flashOnOpponentsTurnReturnsCreatureDuringThatTurnsEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        castSwan();
        resolveTrigger(target);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.FLYING))
                .isEqualTo(1);
    }

    private void castSwan() {
        harness.castFromHand(player1, new SalvationSwan(), "{3}{W}");
    }

    private void resolveTrigger(Permanent target) {
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
