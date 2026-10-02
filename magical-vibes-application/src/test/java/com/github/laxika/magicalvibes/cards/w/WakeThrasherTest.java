package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MerrowLevitator;
import com.github.laxika.magicalvibes.cards.s.SlipperyBogle;
import com.github.laxika.magicalvibes.cards.s.StreamHopper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakeThrasher.class, SlipperyBogle.class, MerrowLevitator.class, StreamHopper.class})
class WakeThrasherTest extends BaseCardTest {

    @Test
    @DisplayName("A permanent you control untapping gives Wake Thrasher +1/+1")
    void anotherPermanentUntappingBoosts() {
        Permanent thrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent bogle = harness.addToBattlefieldAndReturn(player1, new SlipperyBogle());
        bogle.tap();

        advanceToUpkeep(player1);

        assertThat(thrasher.getPowerModifier()).isEqualTo(1);
        assertThat(thrasher.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple permanents untapping in one untap step stack multiple boosts")
    void multipleUntapsStack() {
        Permanent thrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent bogle = harness.addToBattlefieldAndReturn(player1, new SlipperyBogle());
        thrasher.tap();
        bogle.tap();

        advanceToUpkeep(player1);

        assertThat(thrasher.getPowerModifier()).isEqualTo(2);
        assertThat(thrasher.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An effect that untaps a permanent you control also triggers Wake Thrasher")
    void effectDrivenUntapTriggers() {
        Permanent thrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent levitator = addCreatureReady(player1, new MerrowLevitator());
        levitator.tap();
        harness.setHand(player1, List.of(new StreamHopper()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(levitator.isTapped()).isFalse();
        assertThat(thrasher.getPowerModifier()).isEqualTo(1);
        assertThat(thrasher.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtCleanup() {
        Permanent thrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent bogle = harness.addToBattlefieldAndReturn(player1, new SlipperyBogle());
        bogle.tap();

        advanceToUpkeep(player1);
        assertThat(thrasher.getPowerModifier()).isEqualTo(1);

        // Advance into cleanup, emptying the hand so no discard-to-hand-size choice interrupts it.
        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thrasher.getPowerModifier()).isEqualTo(0);
        assertThat(thrasher.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A permanent an opponent controls untapping does not trigger Wake Thrasher")
    void opponentUntapDoesNotTrigger() {
        Permanent thrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        Permanent oppBogle = harness.addToBattlefieldAndReturn(player2, new SlipperyBogle());
        oppBogle.tap();

        advanceToUpkeep(player2);

        assertThat(thrasher.getPowerModifier()).isEqualTo(0);
        assertThat(thrasher.getToughnessModifier()).isEqualTo(0);
    }
}
