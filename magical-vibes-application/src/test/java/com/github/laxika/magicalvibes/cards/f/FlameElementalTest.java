package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlameElementalTest extends BaseCardTest {

    @Test
    @DisplayName("{R}, {T}, Sacrifice: deals damage equal to its power to target creature")
    void dealsPowerDamageToTargetCreature() {
        Permanent flameElemental = addCreatureReady(player1, new FlameElemental());
        Permanent target = addCreatureReady(player2, new AirElemental());

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
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ability cannot be activated without the red mana")
    void requiresRedMana() {
        addCreatureReady(player1, new FlameElemental());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
