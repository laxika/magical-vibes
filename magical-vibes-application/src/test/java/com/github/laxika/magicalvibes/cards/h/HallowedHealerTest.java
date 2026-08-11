package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HallowedHealerTest extends BaseCardTest {

    @Test
    @DisplayName("The basic ability prevents 2 damage to a target player")
    void preventsTwoDamageToPlayer() {
        addHealerReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("The threshold ability prevents 4 damage to a target creature")
    void thresholdPreventsFourDamageToCreature() {
        addHealerReady();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(4);
    }

    @Test
    @DisplayName("The threshold ability cannot be activated with fewer than seven graveyard cards")
    void thresholdRequiresSevenGraveyardCards() {
        addHealerReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");
    }

    private void addHealerReady() {
        harness.addToBattlefield(player1, new HallowedHealer());
        Permanent healer = findPermanent(player1, "Hallowed Healer");
        healer.setSummoningSick(false);
    }
}
