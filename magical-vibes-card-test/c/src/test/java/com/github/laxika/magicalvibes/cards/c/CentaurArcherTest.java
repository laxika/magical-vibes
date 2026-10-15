package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.k.KjeldoranSkyknight;
import com.github.laxika.magicalvibes.cards.w.WindSpirit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CentaurArcher.class, BalduvianBears.class, KjeldoranSkyknight.class, WindSpirit.class})
class CentaurArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target creature with flying")
    void dealsDamageToFlyingCreature() {
        Permanent archer = addCreatureReady(player1, new CentaurArcher());

        Permanent skyknight = harness.addToBattlefieldAndReturn(player2, new KjeldoranSkyknight());

        harness.activateAbility(player1, 0, null, skyknight.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Kjeldoran Skyknight");
        assertThat(archer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals exactly 1 damage without destroying a larger flying creature")
    void dealsExactlyOneDamageToLargerFlyingCreature() {
        addCreatureReady(player1, new CentaurArcher());
        Permanent windSpirit = harness.addToBattlefieldAndReturn(player2, new WindSpirit());

        harness.activateAbility(player1, 0, null, windSpirit.getId());
        harness.passBothPriorities();

        assertThat(windSpirit.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Wind Spirit");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addCreatureReady(player1, new CentaurArcher());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }

    @Test
    @DisplayName("Can target its controller's flying creature")
    void canTargetOwnFlyingCreature() {
        addCreatureReady(player1, new CentaurArcher());
        Permanent skyknight = harness.addToBattlefieldAndReturn(player1, new KjeldoranSkyknight());

        harness.activateAbility(player1, 0, null, skyknight.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kjeldoran Skyknight");
        harness.assertNotOnBattlefield(player1, "Kjeldoran Skyknight");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent archer = harness.addToBattlefieldAndReturn(player1, new CentaurArcher());
        Permanent windSpirit = harness.addToBattlefieldAndReturn(player2, new WindSpirit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, windSpirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(archer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(windSpirit.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateAgainWhileTapped() {
        Permanent archer = addCreatureReady(player1, new CentaurArcher());
        Permanent windSpirit = harness.addToBattlefieldAndReturn(player2, new WindSpirit());

        harness.activateAbility(player1, 0, null, windSpirit.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, windSpirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(archer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(windSpirit.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Wind Spirit");
    }
}
