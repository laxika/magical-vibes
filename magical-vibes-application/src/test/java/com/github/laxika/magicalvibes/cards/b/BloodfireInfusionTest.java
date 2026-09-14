package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.l.LightningAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodfireInfusion.class, Dodecapod.class, GaeasSkyfolk.class, LightningAngel.class})
class BloodfireInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices the enchanted creature and deals its power to each creature")
    void sacrificesEnchantedCreatureAndDealsItsPowerToEachCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new Dodecapod());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BloodfireInfusion());
        aura.setAttachedTo(host.getId());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player2, new LightningAngel());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dodecapod");
        harness.assertInGraveyard(player1, "Bloodfire Infusion");
        harness.assertInGraveyard(player1, "Gaea's Skyfolk");
        assertThat(largeCreature).isIn(gd.playerBattlefields.get(player2.getId()));
        assertThat(largeCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifice cost selects the enchanted creature and does not damage players")
    void sacrificesOnlyTheEnchantedCreatureAndDoesNotDamagePlayers() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new Dodecapod());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new LightningAngel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BloodfireInfusion());
        aura.setAttachedTo(host.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dodecapod");
        assertThat(otherCreature).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(otherCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can enchant only a creature controlled by its caster")
    void cannotEnchantOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new LightningAngel());
        harness.setHand(player1, List.of(new BloodfireInfusion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
