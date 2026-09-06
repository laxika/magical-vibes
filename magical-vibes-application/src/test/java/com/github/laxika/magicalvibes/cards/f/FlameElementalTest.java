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
}
