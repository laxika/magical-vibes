package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AegisOfTheMeek.class, BalduvianBears.class, FyndhornElves.class})
class AegisOfTheMeekTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives a 1/1 creature +1/+2 until end of turn")
    void boostsOneOneCreature() {
        Permanent aegis = harness.addToBattlefieldAndReturn(player1, new AegisOfTheMeek());
        Permanent elves = addCreatureReady(player1, new FyndhornElves());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThat(aegis.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefieldAndReturn(player1, new AegisOfTheMeek());
        Permanent elves = addCreatureReady(player1, new FyndhornElves());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can target an opponent's 1/1 creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new AegisOfTheMeek());
        Permanent elves = addCreatureReady(player2, new FyndhornElves());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability cannot target a creature that is not 1/1")
    void cannotTargetNonOneOneCreature() {
        harness.addToBattlefieldAndReturn(player1, new AegisOfTheMeek());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a 1/1 creature");
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new AegisOfTheMeek());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AegisOfTheMeek());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability requires one generic mana to activate")
    void requiresGenericManaToActivate() {
        Permanent aegis = harness.addToBattlefieldAndReturn(player1, new AegisOfTheMeek());
        Permanent elves = addCreatureReady(player1, new FyndhornElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(aegis.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target must still be a 1/1 creature when the ability resolves")
    void targetMustStillBeOneOneOnResolution() {
        harness.addToBattlefieldAndReturn(player1, new AegisOfTheMeek());
        harness.addToBattlefieldAndReturn(player1, new AegisOfTheMeek());
        Permanent elves = addCreatureReady(player1, new FyndhornElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.activateAbility(player1, 1, null, elves.getId());

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(3);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(3);
    }
}
