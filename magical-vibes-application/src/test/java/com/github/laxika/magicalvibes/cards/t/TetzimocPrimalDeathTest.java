package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TetzimocPrimalDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Its hand ability reveals the card and puts a prey counter on a creature")
    void putsPreyCounterFromHand() {
        harness.setHand(player1, List.of(new TetzimocPrimalDeath()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PREY)).isEqualTo(1);
        harness.assertInHand(player1, "Tetzimoc, Primal Death");
    }

    @Test
    @DisplayName("Its enter-the-battlefield ability destroys marked opposing creatures only")
    void destroysMarkedOpposingCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownBears.setCounterCount(CounterType.PREY, 1);
        Permanent markedOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        markedOpponent.setCounterCount(CounterType.PREY, 1);
        Permanent unmarkedOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TetzimocPrimalDeath()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(markedOpponent).contains(unmarkedOpponent);
    }

    @Test
    @DisplayName("Its hand ability can be activated only during its controller's turn")
    void handAbilityRequiresItsControllersTurn() {
        harness.setHand(player1, List.of(new TetzimocPrimalDeath()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This ability can only be activated during your turn");
        harness.assertInHand(player1, "Tetzimoc, Primal Death");
    }
}
