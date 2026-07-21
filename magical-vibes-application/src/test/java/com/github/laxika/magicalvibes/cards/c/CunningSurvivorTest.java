package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CunningSurvivorTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives +1/+0 and makes this creature unblockable")
    void cyclingBoostsAndUnblockable() {
        harness.addToBattlefield(player1, new CunningSurvivor());
        // Cycling is a discard (CR 702.29e), so cycling Censor triggers the ability.
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the trigger

        Permanent survivor = getSurvivor();
        assertThat(survivor.getPowerModifier()).isEqualTo(1);
        assertThat(survivor.getToughnessModifier()).isEqualTo(0);
        assertThat(survivor.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Each discard stacks another +1/+0")
    void discardsStackPower() {
        harness.addToBattlefield(player1, new CunningSurvivor());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent survivor = getSurvivor();
        assertThat(survivor.getPowerModifier()).isEqualTo(2);
        assertThat(survivor.getToughnessModifier()).isEqualTo(0);
        assertThat(survivor.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The boost and unblockable wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CunningSurvivor());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent survivor = getSurvivor();
        assertThat(survivor.getPowerModifier()).isEqualTo(1);
        assertThat(survivor.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(survivor.getPowerModifier()).isEqualTo(0);
        assertThat(survivor.getToughnessModifier()).isEqualTo(0);
        assertThat(survivor.isCantBeBlocked()).isFalse();
    }

    private Permanent getSurvivor() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cunning Survivor"))
                .findFirst()
                .orElseThrow();
    }
}
