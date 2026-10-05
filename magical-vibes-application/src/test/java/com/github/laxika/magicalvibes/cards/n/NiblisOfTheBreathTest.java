package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HauntedFengraf;
import com.github.laxika.magicalvibes.cards.s.SomberwaldDryad;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NiblisOfTheBreath.class, SomberwaldDryad.class, HauntedFengraf.class})
class NiblisOfTheBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack and taps Niblis")
    void activatingPutsAbilityOnStackAndTapsNiblis() {
        Permanent niblis = addCreatureReady(player1, new NiblisOfTheBreath());
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(niblis.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolves by tapping an untapped target creature")
    void tapsUntappedCreature() {
        addCreatureReady(player1, new NiblisOfTheBreath());
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Tap");

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolves by untapping a tapped target creature")
    void untapsTappedCreature() {
        addCreatureReady(player1, new NiblisOfTheBreath());
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Untap");

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target a creature controlled by Niblis's controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new NiblisOfTheBreath());
        Permanent target = addCreatureReady(player1, new SomberwaldDryad());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Tap");

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new NiblisOfTheBreath());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new HauntedFengraf());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new NiblisOfTheBreath());
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent niblis = addCreatureReady(player1, new NiblisOfTheBreath());
        niblis.tap();
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new NiblisOfTheBreath());
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves the battlefield before resolution")
    void fizzlesIfTargetLeavesBattlefield() {
        addCreatureReady(player1, new NiblisOfTheBreath());
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("May decline to tap an untapped target")
    void mayLeaveUntappedCreatureUnchanged() {
        Permanent niblis = addCreatureReady(player1, new NiblisOfTheBreath());
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
        assertThat(niblis.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May decline to untap a tapped target")
    void mayLeaveTappedCreatureUnchanged() {
        addCreatureReady(player1, new NiblisOfTheBreath());
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can choose to tap an already tapped creature without untapping it")
    void canChooseTapForTappedCreature() {
        addCreatureReady(player1, new NiblisOfTheBreath());
        Permanent target = addCreatureReady(player2, new SomberwaldDryad());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Tap");

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May target itself and untap after paying its tap cost")
    void canUntapItself() {
        Permanent niblis = addCreatureReady(player1, new NiblisOfTheBreath());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, niblis.getId());
        assertThat(niblis.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.isAwaitingInput()) {
            harness.handleListChoice(player1, "1");
        }

        assertThat(niblis.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
