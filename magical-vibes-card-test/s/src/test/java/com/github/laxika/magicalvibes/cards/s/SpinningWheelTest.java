package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GarenbrigSquire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinningWheel.class, GarenbrigSquire.class})
class SpinningWheelTest extends BaseCardTest {

    @Test
    void tapAbilityAddsChosenColor() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());

        harness.activateAbility(player1, 0, null, null);

        assertThat(wheel.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void fiveManaTapAbilityTapsTargetCreature() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());
        Permanent target = addCreatureReady(player2, new GarenbrigSquire());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(wheel.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapAbilityCannotTargetNonCreature() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, wheel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void manaAbilityResolvesWithoutUsingTheStackForEveryColor(ManaColor color) {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(wheel.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void creatureTapAbilityPaysItsCostsBeforeResolving() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());
        Permanent target = addCreatureReady(player1, new GarenbrigSquire());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(wheel.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureTapAbilityRequiresFiveMana() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());
        Permanent target = addCreatureReady(player2, new GarenbrigSquire());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wheel.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappingForManaPreventsActivatingEitherTapAbilityAgain() {
        harness.addToBattlefield(player1, new SpinningWheel());
        Permanent target = addCreatureReady(player2, new GarenbrigSquire());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureTapAbilityCanTargetAnAlreadyTappedCreature() {
        harness.addToBattlefield(player1, new SpinningWheel());
        Permanent target = addCreatureReady(player2, new GarenbrigSquire());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureTapAbilityResolvesAfterWheelLeavesTheBattlefield() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());
        Permanent target = addCreatureReady(player2, new GarenbrigSquire());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(wheel);
        gd.playerGraveyards.get(player1.getId()).add(wheel.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureTapAbilityDoesNotRetargetWhenTargetLeavesTheBattlefield() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new SpinningWheel());
        Permanent target = addCreatureReady(player2, new GarenbrigSquire());
        Permanent other = addCreatureReady(player2, new GarenbrigSquire());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(wheel.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
