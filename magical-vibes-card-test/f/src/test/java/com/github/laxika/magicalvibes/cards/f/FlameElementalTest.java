package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameElemental.class, GiantMantis.class, ViashinoWarrior.class, Forest.class})
class FlameElementalTest extends BaseCardTest {

    @Test
    @DisplayName("{R}, {T}, Sacrifice: deals damage equal to its power to target creature")
    void dealsPowerDamageToTargetCreature() {
        Permanent flameElemental = addCreatureReady(player1, new FlameElemental());
        Permanent target = addCreatureReady(player2, new GiantMantis());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Flame Elemental");
        assertThat(flameElemental.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lethal power damage destroys the target creature")
    void lethalDamageDestroysTarget() {
        addCreatureReady(player1, new FlameElemental());
        Permanent target = addCreatureReady(player2, new ViashinoWarrior());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Viashino Warrior");
    }

    @Test
    @DisplayName("Ability cannot be activated without the red mana")
    void requiresRedMana() {
        addCreatureReady(player1, new FlameElemental());
        Permanent target = addCreatureReady(player2, new ViashinoWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses its effective power after sacrificing itself")
    void usesEffectivePowerAfterSacrifice() {
        Permanent flameElemental = addCreatureReady(player1, new FlameElemental());
        flameElemental.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new GiantMantis());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Giant Mantis"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Mantis");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new FlameElemental());
        harness.addToBattlefield(player2, new Forest());
        Permanent target = findPermanent(player2, "Forest");
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, before damage resolves")
    void sacrificesAsActivationCost() {
        addCreatureReady(player1, new FlameElemental());
        Permanent target = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Flame Elemental");
        harness.assertInGraveyard(player1, "Flame Elemental");
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped Flame Elemental cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent source = addCreatureReady(player1, new FlameElemental());
        source.tap();
        Permanent target = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Flame Elemental");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new FlameElemental());
        Permanent target = addCreatureReady(player2, new GiantMantis());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Flame Elemental");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can deal damage to a creature its controller controls")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new FlameElemental());
        Permanent target = addCreatureReady(player1, new GiantMantis());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Flame Elemental");
    }

    @Test
    @DisplayName("Can target itself, but the sacrificed target is gone at resolution")
    void canTargetItself() {
        Permanent source = addCreatureReady(player1, new FlameElemental());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.assertInGraveyard(player1, "Flame Elemental");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flame Elemental");
        assertThat(gd.stack).isEmpty();
        assertThat(source.getMarkedDamage()).isZero();
    }
}
