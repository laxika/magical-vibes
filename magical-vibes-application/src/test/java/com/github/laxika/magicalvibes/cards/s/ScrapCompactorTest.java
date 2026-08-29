package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScrapCompactorTest extends BaseCardTest {

    @Test
    @DisplayName("The damage ability sacrifices Scrap Compactor and deals 3 damage to a creature")
    void dealsDamageToTargetCreature() {
        harness.addToBattlefield(player1, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Scrap Compactor");
    }

    @Test
    @DisplayName("The destruction ability sacrifices Scrap Compactor and destroys a Vehicle")
    void destroysTargetVehicle() {
        harness.addToBattlefield(player1, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scrap Compactor");
        harness.assertInGraveyard(player2, "Dusk Legion Dreadnought");
    }

    @Test
    @DisplayName("The damage ability cannot target a noncreature Vehicle")
    void damageAbilityCannotTargetVehicle() {
        harness.addToBattlefield(player1, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The destruction ability cannot target a land")
    void destructionAbilityCannotTargetLand() {
        harness.addToBattlefield(player1, new ScrapCompactor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
