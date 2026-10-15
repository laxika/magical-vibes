package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoamDryad.class, DevilthornFox.class})
class LoamDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and another creature to add mana of the chosen color")
    void tapsItselfAndAnotherCreatureForMana() {
        Permanent dryad = addCreatureReady(player1, new LoamDryad());
        Permanent creature = addCreatureReady(player1, new DevilthornFox());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(dryad.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without another untapped creature you control")
    void requiresAnotherUntappedCreature() {
        Permanent dryad = addCreatureReady(player1, new LoamDryad());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dryad.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot tap a creature controlled by an opponent")
    void requiresCreatureYouControl() {
        Permanent dryad = addCreatureReady(player1, new LoamDryad());
        addCreatureReady(player2, new DevilthornFox());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dryad.isTapped()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Produces exactly one mana of any chosen color without using the stack")
    void producesOneManaOfEachColor(ManaColor color) {
        Permanent dryad = addCreatureReady(player1, new LoamDryad());
        Permanent creature = addCreatureReady(player1, new DevilthornFox());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(dryad.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The additional creature may have summoning sickness")
    void canTapSummoningSickHelper() {
        Permanent dryad = addCreatureReady(player1, new LoamDryad());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(dryad.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loam Dryad cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new LoamDryad());
        dryad.setSummoningSick(true);
        Permanent creature = addCreatureReady(player1, new DevilthornFox());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dryad.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An already tapped creature cannot pay the additional cost")
    void cannotTapAlreadyTappedHelper() {
        Permanent dryad = addCreatureReady(player1, new LoamDryad());
        Permanent creature = addCreatureReady(player1, new DevilthornFox());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dryad.isTapped()).isFalse();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
