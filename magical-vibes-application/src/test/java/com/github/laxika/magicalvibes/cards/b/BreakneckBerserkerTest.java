package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakneckBerserker.class})
class BreakneckBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield")
    void canAttackTheTurnItEnters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BreakneckBerserker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Haste does not allow attacking while tapped")
    void cannotAttackWhileTapped() {
        var berserker = harness.enterBattlefieldAndReturn(player1, new BreakneckBerserker());
        berserker.setTapped(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
