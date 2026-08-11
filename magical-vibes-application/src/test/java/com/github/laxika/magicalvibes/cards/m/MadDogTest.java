package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MadDogTest extends BaseCardTest {

    private Permanent addMadDog(boolean summoningSick) {
        Permanent perm = new Permanent(new MadDog());
        perm.setSummoningSick(summoningSick);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrifices itself at the end step when it did not attack")
    void sacrificesWhenItDidNotAttack() {
        addMadDog(false);

        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mad Dog");
        harness.assertInGraveyard(player1, "Mad Dog");
    }

    @Test
    @DisplayName("Does not sacrifice itself when it attacked this turn")
    void doesNotSacrificeWhenItAttacked() {
        Permanent madDog = addMadDog(false);
        madDog.setAttackedThisTurn(true);

        advanceToEndStep();

        assertThat(gd.stack).noneMatch(e -> e.getSourcePermanentId().equals(madDog.getId()));
        harness.assertOnBattlefield(player1, "Mad Dog");
    }

    @Test
    @DisplayName("Does not sacrifice itself when it came under control this turn")
    void doesNotSacrificeWhenItCameUnderControl() {
        addMadDog(true);

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mad Dog");
    }
}
