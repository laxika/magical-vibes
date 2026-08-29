package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeldonBattlewagonTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature to get +X/+0 equal to that creature's power")
    void tapsCreatureForPowerBoost() {
        Permanent battlewagon = addReadyCreature(new KeldonBattlewagon());
        Permanent grizzlyBears = addReadyCreature(new GrizzlyBears());

        harness.activateAbility(player1, index(battlewagon), null, null);
        harness.handlePermanentChosen(player1, grizzlyBears.getId());
        harness.passBothPriorities();

        assertThat(grizzlyBears.isTapped()).isTrue();
        assertThat(battlewagon.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, battlewagon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, battlewagon)).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent battlewagon = addReadyCreature(new KeldonBattlewagon());
        Permanent grizzlyBears = addReadyCreature(new GrizzlyBears());

        harness.activateAbility(player1, index(battlewagon), null, null);
        harness.handlePermanentChosen(player1, grizzlyBears.getId());
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
        Permanent battlewagon = addReadyCreature(new KeldonBattlewagon());

        harness.activateAbility(player1, index(battlewagon), null, null);
        harness.passBothPriorities();

        assertThat(battlewagon.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, battlewagon)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate while no creature is untapped")
    void cannotActivateWithoutAnUntappedCreature() {
        Permanent battlewagon = addReadyCreature(new KeldonBattlewagon());
        battlewagon.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, index(battlewagon), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent battlewagon = harness.addToBattlefieldAndReturn(player2, new KeldonBattlewagon());
        battlewagon.setSummoningSick(false);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        Permanent battlewagon = addReadyCreature(new KeldonBattlewagon());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(index(battlewagon)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Keldon Battlewagon");
        harness.assertInGraveyard(player1, "Keldon Battlewagon");
    }

    private int index(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
