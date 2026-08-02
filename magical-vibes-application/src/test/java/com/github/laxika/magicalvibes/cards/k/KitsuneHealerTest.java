package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KitsuneHealerTest extends BaseCardTest {

    private void addHealerReady() {
        harness.addToBattlefield(player1, new KitsuneHealer());
        Permanent healer = findPermanent(player1, "Kitsune Healer");
        healer.setSummoningSick(false);
    }

    @Test
    @DisplayName("Prevents the next 1 damage to any target")
    void preventsNextDamageToAnyTarget() {
        addHealerReady();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents all damage to a target legendary creature")
    void preventsAllDamageToLegendaryCreature() {
        addHealerReady();
        harness.addToBattlefield(player2, new KokushoTheEveningStar());

        UUID targetId = harness.getPermanentId(player2, "Kokusho, the Evening Star");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.creaturesWithAllDamagePrevented).contains(targetId);
    }

    @Test
    @DisplayName("Rejects a nonlegendary creature for the second ability")
    void rejectsNonlegendaryCreature() {
        addHealerReady();
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
