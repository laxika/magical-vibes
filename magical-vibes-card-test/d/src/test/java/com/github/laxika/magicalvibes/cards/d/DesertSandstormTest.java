package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.cards.w.WeiScout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesertSandstorm.class, AlertShuInfantry.class, WeiScout.class})
class DesertSandstormTest extends BaseCardTest {

    @Test
    @DisplayName("Desert Sandstorm deals 1 damage to each creature on both sides")
    void dealsOneDamageToEachCreature() {
        addCreatureReady(player1, new WeiScout());           // 1/1 dies
        addCreatureReady(player2, new AlertShuInfantry());   // 2/2 survives

        harness.castFromHand(player1, new DesertSandstorm(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wei Scout");
        harness.assertOnBattlefield(player2, "Alert Shu Infantry");
        assertThat(findPermanent(player2, "Alert Shu Infantry").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Desert Sandstorm does not damage players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new DesertSandstorm(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Repeated Desert Sandstorms accumulate lethal damage on creatures")
    void repeatedDamageIsLethal() {
        addCreatureReady(player1, new AlertShuInfantry());
        addCreatureReady(player2, new AlertShuInfantry());

        harness.castFromHand(player1, new DesertSandstorm(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alert Shu Infantry");
        harness.assertOnBattlefield(player2, "Alert Shu Infantry");

        harness.castFromHand(player1, new DesertSandstorm(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alert Shu Infantry");
        harness.assertNotOnBattlefield(player2, "Alert Shu Infantry");
        harness.assertInGraveyard(player1, "Alert Shu Infantry");
        harness.assertInGraveyard(player2, "Alert Shu Infantry");
    }

    @Test
    @DisplayName("Desert Sandstorm damages creatures present at resolution")
    void damagesCreatureEnteringAfterCast() {
        harness.castFromHand(player1, new DesertSandstorm(), "{2}{R}");
        harness.addToBattlefield(player2, new WeiScout());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wei Scout");
        harness.assertInGraveyard(player2, "Wei Scout");
        harness.assertInGraveyard(player1, "Desert Sandstorm");
    }

    @Test
    @DisplayName("Cannot cast Desert Sandstorm without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new DesertSandstorm()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }
}
