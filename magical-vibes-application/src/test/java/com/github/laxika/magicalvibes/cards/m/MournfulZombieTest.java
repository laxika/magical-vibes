package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MournfulZombie.class})
class MournfulZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains 1 life and the Zombie becomes tapped")
    void targetPlayerGainsLife() {
        Permanent zombie = addCreatureReady(player1, new MournfulZombie());
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 1);
        assertThat(zombie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Its controller can be the target")
    void controllerCanBeTargeted() {
        addCreatureReady(player1, new MournfulZombie());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
    }
}
