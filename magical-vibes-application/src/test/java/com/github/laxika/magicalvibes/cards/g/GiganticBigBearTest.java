package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiganticBigBear.class, Cancel.class, Shock.class})
class GiganticBigBearTest extends BaseCardTest {

    @Test
    @DisplayName("Gigantic Big Bear cannot be countered")
    void cannotBeCountered() {
        GiganticBigBear bear = new GiganticBigBear();
        harness.setHand(player1, List.of(bear));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gigantic Big Bear");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("An opponent cannot target Gigantic Big Bear")
    void opponentCannotTargetWithSpell() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GiganticBigBear());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Haste allows Gigantic Big Bear to attack immediately")
    void hasteAllowsAttackingImmediately() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GiganticBigBear());

        declareAttackers(List.of(0));

        assertThat(bear.isTapped()).isTrue();
    }
}
