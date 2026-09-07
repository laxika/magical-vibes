package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BlinkDog.class)
class BlinkDogTest extends BaseCardTest {

    @Test
    void teleportPhasesBlinkDogOut() {
        Permanent dog = addDogReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dog);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(dog);
    }

    @Test
    void blinkDogPhasesBackInDuringItsControllersNextUntapStep() {
        Permanent dog = addDogReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(dog);

        advanceTurn();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dog);

        advanceTurn();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dog);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(dog);
    }

    private Permanent addDogReady(Player player) {
        Permanent perm = new Permanent(new BlinkDog());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
