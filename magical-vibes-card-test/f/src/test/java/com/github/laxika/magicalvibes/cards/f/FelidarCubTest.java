package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RetreatToKazandu;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FelidarCub.class, RetreatToKazandu.class})
class FelidarCubTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Felidar Cub and destroys target enchantment")
    void destroysTargetEnchantment() {
        addCreatureReady(player1, new FelidarCub());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RetreatToKazandu());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Felidar Cub");
        harness.assertNotOnBattlefield(player1, "Felidar Cub");
        harness.assertOnBattlefield(player2, "Retreat to Kazandu");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Felidar Cub");
        harness.assertInGraveyard(player2, "Retreat to Kazandu");
    }

    @Test
    @DisplayName("Can activate with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new FelidarCub());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RetreatToKazandu());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Felidar Cub");
        harness.assertInGraveyard(player2, "Retreat to Kazandu");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new FelidarCub());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FelidarCub());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Felidar Cub");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can destroy an enchantment you control")
    void destroysOwnEnchantment() {
        harness.addToBattlefield(player1, new FelidarCub());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RetreatToKazandu());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Felidar Cub");
        harness.assertInGraveyard(player1, "Retreat to Kazandu");
    }

    @Test
    @DisplayName("Sacrifice costs remain paid when the target leaves before resolution")
    void targetDestroyedInResponseDoesNotRefundSacrifice() {
        harness.addToBattlefield(player1, new FelidarCub());
        harness.addToBattlefield(player1, new FelidarCub());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RetreatToKazandu());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Retreat to Kazandu");
    }
}
