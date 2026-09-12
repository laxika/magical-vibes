package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({KeldonBattlewagon.class, PygmyRazorback.class})
class KeldonBattlewagonTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature to get +X/+0 equal to that creature's power")
    void tapsCreatureForPowerBoost() {
        Permanent battlewagon = addCreatureReady(player1, new KeldonBattlewagon());
        Permanent pygmyRazorback = addCreatureReady(player1, new PygmyRazorback());

        harness.activateAbility(player1, index(battlewagon), null, null);
        harness.handlePermanentChosen(player1, pygmyRazorback.getId());
        harness.passBothPriorities();

        assertThat(pygmyRazorback.isTapped()).isTrue();
        assertThat(battlewagon.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, battlewagon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, battlewagon)).isEqualTo(3);
    }

    @Test
    @DisplayName("Uses the tapped creature's power as the ability resolves")
    void usesTappedCreaturePowerAtResolution() {
        Permanent battlewagon = addCreatureReady(player1, new KeldonBattlewagon());
        Permanent pygmyRazorback = addCreatureReady(player1, new PygmyRazorback());

        harness.activateAbility(player1, index(battlewagon), null, null);
        harness.handlePermanentChosen(player1, pygmyRazorback.getId());
        pygmyRazorback.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, battlewagon)).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent battlewagon = addCreatureReady(player1, new KeldonBattlewagon());
        Permanent pygmyRazorback = addCreatureReady(player1, new PygmyRazorback());

        harness.activateAbility(player1, index(battlewagon), null, null);
        harness.handlePermanentChosen(player1, pygmyRazorback.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, battlewagon)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, battlewagon)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can tap itself when it is the only untapped creature")
    void canTapItself() {
        Permanent battlewagon = addCreatureReady(player1, new KeldonBattlewagon());

        harness.activateAbility(player1, index(battlewagon), null, null);
        harness.passBothPriorities();

        assertThat(battlewagon.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, battlewagon)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate while no creature is untapped")
    void cannotActivateWithoutAnUntappedCreature() {
        Permanent battlewagon = addCreatureReady(player1, new KeldonBattlewagon());
        battlewagon.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, index(battlewagon), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use an opponent's untapped creature to pay")
    void cannotUseOpponentsCreatureToPay() {
        Permanent battlewagon = addCreatureReady(player1, new KeldonBattlewagon());
        battlewagon.tap();
        addCreatureReady(player2, new PygmyRazorback());

        assertThatThrownBy(() -> harness.activateAbility(player1, index(battlewagon), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent battlewagon = harness.addToBattlefieldAndReturn(player2, new KeldonBattlewagon());
        battlewagon.setSummoningSick(false);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Is sacrificed at end of combat after attacking")
    void sacrificedAtEndOfCombatAfterAttacking() {
        Permanent battlewagon = addCreatureReady(player1, new KeldonBattlewagon());

        declareAttackers(List.of(index(battlewagon)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Keldon Battlewagon");
        harness.assertInGraveyard(player1, "Keldon Battlewagon");
    }

    private int index(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

}
