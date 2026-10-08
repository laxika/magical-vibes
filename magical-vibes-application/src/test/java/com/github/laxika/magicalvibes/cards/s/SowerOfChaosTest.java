package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SowerOfChaos.class, BearCub.class, Forest.class})
class SowerOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability makes target creature unable to block this turn")
    void activatedAbilityMakesTargetUnableToBlock() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        readySower();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Target creature cannot declare as a blocker after the ability resolves")
    void targetCannotDeclareAsBlocker() {
        addCreatureReady(player1, new BearCub());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BearCub());
        readySower();

        harness.activateAbility(player1, 1, 0, null, blocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't-block effect wears off at end of turn")
    void cantBlockEffectWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        readySower();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        readySower();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Sower can activate repeatedly and target itself")
    void tappedSummoningSickSowerCanActivateRepeatedly() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Permanent unaffected = harness.addToBattlefieldAndReturn(player2, new BearCub());
        readySower();
        Permanent sower = findPermanent(player1, "Sower of Chaos");
        sower.tap();
        sower.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 0, 0, null, sower.getId());
        resolveAllTriggers();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(sower.isCantBlockThisTurn()).isTrue();
        assertThat(unaffected.isCantBlockThisTurn()).isFalse();
        assertThat(sower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can be activated during an opponent's combat")
    void canActivateDuringOpponentsCombat() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearCub());
        readySower();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Activating requires red mana even when enough generic mana is available")
    void cannotActivateWithoutRedMana() {
        Permanent sower = harness.addToBattlefieldAndReturn(player1, new SowerOfChaos());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, sower.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(sower.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Activating requires the full three mana")
    void cannotActivateWithOnlyTwoMana() {
        Permanent sower = harness.addToBattlefieldAndReturn(player1, new SowerOfChaos());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, sower.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(sower.isCantBlockThisTurn()).isFalse();
    }

    private void readySower() {
        harness.addToBattlefield(player1, new SowerOfChaos());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
