package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EbonDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Controller loses 1 life when any player casts a spell")
    void controllerLosesLifeWhenAnyPlayerCastsSpell() {
        harness.addToBattlefield(player1, new EbonDrake());
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int drakeControllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int casterLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(drakeControllerLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(casterLifeBefore);
    }

    @Test
    @DisplayName("Controller loses 1 life when they cast a spell")
    void controllerLosesLifeWhenTheyCastSpell() {
        harness.addToBattlefield(player1, new EbonDrake());
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }
}
