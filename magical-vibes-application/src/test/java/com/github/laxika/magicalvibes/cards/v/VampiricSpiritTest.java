package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VampiricSpirit.class)
class VampiricSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the creature spell puts the ETB trigger on the stack")
    void resolvingPutsEtbOnStack() {
        castVampiricSpirit();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB trigger makes the controller lose 4 life")
    void etbMakesControllerLose4Life() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castVampiricSpirit();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's life is unaffected by the ETB trigger")
    void opponentLifeUnaffected() {
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        castVampiricSpirit();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    private void castVampiricSpirit() {
        harness.castFromHand(player1, new VampiricSpirit(), "{2}{B}{B}");
    }
}
