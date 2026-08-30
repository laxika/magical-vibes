package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevourerOfMemory.class, Forest.class, GrizzlyBears.class, Millstone.class})
class DevourerOfMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Milling a card gives Devourer of Memory +1/+1 and makes it unblockable")
    void millingCardBoostsAndMakesUnblockable() {
        Permanent devourer = addCreatureReady(player1, new DevourerOfMemory());
        harness.setLibrary(player1, List.of(new Forest()));
        activateMillAbility();

        assertThat(devourer.getEffectivePower()).isEqualTo(3);
        assertThat(devourer.getEffectiveToughness()).isEqualTo(2);
        assertThat(devourer.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Milling multiple cards in one event triggers only once")
    void multipleCardsInOneMillEventTriggerOnce() {
        Permanent devourer = addCreatureReady(player1, new DevourerOfMemory());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(devourer.getEffectivePower()).isEqualTo(3);
        assertThat(devourer.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost and unblockable effect wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent devourer = addCreatureReady(player1, new DevourerOfMemory());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        activateMillAbility();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(devourer.getEffectivePower()).isEqualTo(2);
        assertThat(devourer.getEffectiveToughness()).isEqualTo(1);
        assertThat(devourer.isCantBeBlocked()).isFalse();
    }

    private void activateMillAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
    }
}
