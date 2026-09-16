package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MadDog.class, Persuasion.class})
class MadDogTest extends BaseCardTest {

    private Permanent addMadDog(boolean summoningSick) {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new MadDog());
        perm.setSummoningSick(summoningSick);
        return perm;
    }

    private void advanceToEndStep() {
        advanceToEndStep(player1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
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

    @Test
    @DisplayName("Does not sacrifice when it came under its controller's control this turn")
    void doesNotSacrificeWhenItCameUnderControlThroughControlChange() {
        Permanent madDog = harness.addToBattlefieldAndReturn(player2, new MadDog());
        madDog.setSummoningSick(false);

        harness.setHand(player1, List.of(new Persuasion()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, madDog.getId());
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mad Dog");
    }

    @Test
    @DisplayName("Triggers only during its controller's end step")
    void triggersOnlyDuringItsControllersEndStep() {
        addMadDog(false);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mad Dog");
    }
}
