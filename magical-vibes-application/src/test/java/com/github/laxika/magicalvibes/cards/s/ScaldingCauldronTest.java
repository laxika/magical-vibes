package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScaldingCauldron.class, AirElemental.class})
class ScaldingCauldronTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target creature")
    void dealsThreeDamageToTargetCreature() {
        harness.addToBattlefield(player1, new ScaldingCauldron());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent target = findPermanent(player2, "Air Elemental");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Is sacrificed as the ability's activation cost")
    void isSacrificedAsCost() {
        harness.addToBattlefield(player1, new ScaldingCauldron());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent target = findPermanent(player2, "Air Elemental");
        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Scalding Cauldron");
        harness.assertInGraveyard(player1, "Scalding Cauldron");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new ScaldingCauldron());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
