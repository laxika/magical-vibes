package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiotControlTest extends BaseCardTest {

    private void castRiotControl() {
        harness.setHand(player2, List.of(new RiotControl()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains 1 life for each creature opponents control")
    void gainsLifePerOpponentCreature() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castRiotControl();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains no life when opponents control no creatures")
    void gainsNoLifeWithoutOpponentCreatures() {
        harness.setLife(player2, 20);

        castRiotControl();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents damage that would be dealt to the controller")
    void preventsDamageToController() {
        harness.setLife(player2, 20);

        castRiotControl();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage dealt to creatures the controller controls")
    void doesNotPreventDamageToControlledCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRiotControl();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> "Grizzly Bears".equals(card.getName()));
    }

    @Test
    @DisplayName("The prevention wears off at end of turn")
    void preventionWearsOff() {
        harness.setLife(player2, 20);

        castRiotControl();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
