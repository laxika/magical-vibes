package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.cards.b.BlessingOfLeeches;
import com.github.laxika.magicalvibes.cards.c.ClashOfRealities;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuraBarbs.class, BileUrchin.class, BlessingOfLeeches.class,
        ClashOfRealities.class, GnarledMass.class})
class AuraBarbsTest extends BaseCardTest {

    private void castAuraBarbs() {
        harness.setHand(player1, List.of(new AuraBarbs()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void attachBlessingOfLeeches(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new BlessingOfLeeches());
        aura.setAttachedTo(creature.getId());
        harness.getGameData().playerBattlefields.get(controller.getId()).add(aura);
    }

    @Test
    @DisplayName("Each enchantment deals 2 damage to its controller, stacking per enchantment")
    void damagesEachEnchantmentController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new ClashOfRealities());
        harness.addToBattlefield(player2, new ClashOfRealities());

        castAuraBarbs();

        GameData gd = harness.getGameData();
        // Two enchantments each deal 2 to their controller; the caster controls none.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An Aura attached to a creature damages both its controller and that creature")
    void auraDamagesControllerAndEnchantedCreature() {
        harness.setLife(player2, 20);
        Permanent urchin = harness.addToBattlefieldAndReturn(player2, new BileUrchin()); // 1/1
        attachBlessingOfLeeches(player2, urchin);

        castAuraBarbs();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player2, "Bile Urchin");
    }

    @Test
    @DisplayName("An Aura damages its controller and a creature controlled by another player")
    void auraDamagesCreatureControlledByOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BileUrchin()); // 1/1
        attachBlessingOfLeeches(player1, creature);

        castAuraBarbs();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player2, "Bile Urchin");
    }

    @Test
    @DisplayName("Each Aura attached to a creature deals damage separately")
    void eachAttachedAuraDealsDamageSeparately() {
        harness.setLife(player2, 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GnarledMass()); // 3/3
        attachBlessingOfLeeches(player2, creature);
        attachBlessingOfLeeches(player2, creature);

        castAuraBarbs();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertNotOnBattlefield(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("Damage is symmetric — the caster's own enchantments hit them too")
    void damagesCasterOwnEnchantments() {
        harness.setLife(player1, 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GnarledMass()); // 3/3 survives
        attachBlessingOfLeeches(player1, creature);

        castAuraBarbs();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Gnarled Mass");
    }

    @Test
    @DisplayName("Does nothing when no enchantments are on the battlefield")
    void noEnchantmentsNoDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new BileUrchin());

        castAuraBarbs();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Bile Urchin");
    }
}
