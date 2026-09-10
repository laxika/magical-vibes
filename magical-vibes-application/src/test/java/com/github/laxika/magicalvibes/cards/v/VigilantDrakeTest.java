package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(VigilantDrake.class)
class VigilantDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts the untap ability on the stack")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new VigilantDrake());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ability untaps Vigilant Drake")
    void resolvingAbilityUntapsSelf() {
        Permanent drake = addCreatureReady(player1, new VigilantDrake());
        drake.tap();
        assertThat(drake.isTapped()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(drake.isTapped()).isFalse();
    }

    @Test
    void resolvingAbilityUntapsOnlyItsSource() {
        Permanent source = addCreatureReady(player1, new VigilantDrake());
        Permanent otherDrake = addCreatureReady(player1, new VigilantDrake());
        source.tap();
        otherDrake.tap();

        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isFalse();
        assertThat(otherDrake.isTapped()).isTrue();
    }

    @Test
    void canActivateWhileSummoningSick() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new VigilantDrake());
        assertThat(drake.isSummoningSick()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(drake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating ability consumes {2}{U}")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new VigilantDrake());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new VigilantDrake());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
