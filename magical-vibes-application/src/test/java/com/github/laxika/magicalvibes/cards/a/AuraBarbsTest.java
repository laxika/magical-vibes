package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuraBarbsTest extends BaseCardTest {

    private void castAuraBarbs() {
        harness.setHand(player1, List.of(new AuraBarbs()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent attachPacifism(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(creature.getId());
        harness.getGameData().playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Each enchantment deals 2 damage to its controller, stacking per enchantment")
    void damagesEachEnchantmentController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new HowlingMine());
        harness.addToBattlefield(player2, new HowlingMine());

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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2
        attachPacifism(player2, bears);

        castAuraBarbs();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage is symmetric — the caster's own enchantments hit them too")
    void damagesCasterOwnEnchantments() {
        harness.setLife(player1, 20);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel()); // 4/4 survives
        attachPacifism(player1, angel);

        castAuraBarbs();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Does nothing when no enchantments are on the battlefield")
    void noEnchantmentsNoDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAuraBarbs();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
