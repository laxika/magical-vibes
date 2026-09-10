package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DauthiSlayer;
import com.github.laxika.magicalvibes.cards.g.GiantStrength;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Shadowstorm.class, DauthiSlayer.class, GiantStrength.class, MoggConscripts.class})
class ShadowstormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to every creature with shadow, regardless of controller")
    void damagesAllShadowCreatures() {
        harness.addToBattlefield(player1, new DauthiSlayer());
        harness.addToBattlefield(player2, new DauthiSlayer());

        harness.castFromHand(player1, new Shadowstorm(), "{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dauthi Slayer");
        harness.assertNotOnBattlefield(player2, "Dauthi Slayer");
    }

    @Test
    @DisplayName("Creatures without shadow are untouched")
    void doesNotDamageNonShadowCreatures() {
        harness.addToBattlefield(player2, new MoggConscripts());

        harness.castFromHand(player1, new Shadowstorm(), "{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mogg Conscripts");
    }

    @Test
    @DisplayName("A shadow creature with toughness greater than 2 survives")
    void toughShadowCreatureSurvives() {
        harness.addToBattlefield(player2, new DauthiSlayer());
        var shadowCreatureId = harness.getPermanentId(player2, "Dauthi Slayer");

        harness.setHand(player1, List.of(new GiantStrength()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, shadowCreatureId);
        harness.passBothPriorities();

        harness.castFromHand(player1, new Shadowstorm(), "{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dauthi Slayer");
    }

    @Test
    @DisplayName("Players take no damage")
    void doesNotDamagePlayers() {
        harness.castFromHand(player1, new Shadowstorm(), "{R}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
