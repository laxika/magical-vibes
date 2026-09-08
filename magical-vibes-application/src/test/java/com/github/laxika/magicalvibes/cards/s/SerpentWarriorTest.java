package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SerpentWarrior.class)
class SerpentWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        castSerpentWarrior();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Serpent Warrior");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB causes controller to lose 3 life")
    void etbLoses3Life() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castSerpentWarrior();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore - 3);
    }

    @Test
    @DisplayName("ETB does not affect the opponent's life")
    void etbDoesNotAffectOpponentsLife() {
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        castSerpentWarrior();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        harness.assertLife(player2, opponentLifeBefore);
    }

    private void castSerpentWarrior() {
        harness.castFromHand(player1, new SerpentWarrior(), "{2}{B}");
    }
}
