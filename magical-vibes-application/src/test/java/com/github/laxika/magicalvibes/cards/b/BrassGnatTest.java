package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BrassGnat.class)
class BrassGnatTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Brass Gnat does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent gnat = addGnat(true);

        advanceToUpkeep(player1);

        assertThat(gnat.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {1} during upkeep untaps Brass Gnat")
    void payingUntapsGnat() {
        Permanent gnat = addGnat(true);
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gnat.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves Brass Gnat tapped")
    void decliningLeavesGnatTapped() {
        Permanent gnat = addGnat(true);
        advanceToUpkeep(player1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gnat.isTapped()).isTrue();
    }

    private Permanent addGnat(boolean tapped) {
        Permanent gnat = harness.addToBattlefieldAndReturn(player1, new BrassGnat());
        gnat.setSummoningSick(false);
        if (tapped) {
            gnat.tap();
        }
        return gnat;
    }
}
