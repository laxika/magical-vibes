package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrassGnat.class})
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

    @Test
    @DisplayName("Brass Gnat stays tapped when its controller cannot pay")
    void cannotPayLeavesGnatTapped() {
        Permanent gnat = addGnat(true);
        advanceToUpkeep(player1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gnat.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Brass Gnat does not trigger during its opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent gnat = addGnat(true);

        advanceToUpkeep(player2);

        assertThat(gnat.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Paying colorless mana untaps only the triggering Brass Gnat")
    void paymentUntapsOnlySource() {
        Permanent gnat = addGnat(true);
        Permanent opposingGnat = harness.addToBattlefieldAndReturn(player2, new BrassGnat());
        opposingGnat.tap();
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gnat.isTapped()).isFalse();
        assertThat(opposingGnat.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An untapped Brass Gnat still offers the optional upkeep payment")
    void untappedGnatStillTriggers() {
        Permanent gnat = addGnat(false);
        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gnat.isTapped()).isFalse();
    }

    private Permanent addGnat(boolean tapped) {
        Permanent gnat = addCreatureReady(player1, new BrassGnat());
        if (tapped) {
            gnat.tap();
        }
        return gnat;
    }
}
