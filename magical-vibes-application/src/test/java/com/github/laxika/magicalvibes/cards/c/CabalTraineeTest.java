package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Brawn;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalTrainee.class, Brawn.class, KrosanVerge.class})
class CabalTraineeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Cabal Trainee weakens the target creature")
    void sacrificesAndWeakensTargetCreature() {
        harness.addToBattlefield(player1, new CabalTrainee());
        harness.addToBattlefield(player2, new Brawn());
        UUID targetId = harness.getPermanentId(player2, "Brawn");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cabal Trainee");
        harness.assertInGraveyard(player1, "Cabal Trainee");
        Permanent brawn = findPermanent(player2, "Brawn");
        assertThat(brawn.getEffectivePower()).isEqualTo(1);
        assertThat(brawn.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CabalTrainee());
        harness.addToBattlefield(player2, new Brawn());
        UUID targetId = harness.getPermanentId(player2, "Brawn");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent brawn = findPermanent(player2, "Brawn");
        assertThat(brawn.getEffectivePower()).isEqualTo(3);
        assertThat(brawn.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new CabalTrainee());
        harness.addToBattlefield(player1, new Brawn());
        UUID targetId = harness.getPermanentId(player1, "Brawn");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent brawn = findPermanent(player1, "Brawn");
        assertThat(brawn.getEffectivePower()).isEqualTo(1);
        assertThat(brawn.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new CabalTrainee());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());
        UUID targetId = land.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Cabal Trainee");
        harness.assertNotInGraveyard(player1, "Cabal Trainee");
    }
}
