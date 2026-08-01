package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SphereOfSafetyTest extends BaseCardTest {

    @Test
    @DisplayName("A lone Sphere taxes {1} per attacker — it counts itself")
    void loneSphereTaxesOne() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        addNonSickCreature(player2, new GrizzlyBears());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player2, List.of(0));

        // Combat auto-resolves (no blockers), so only the paid tax is observable here.
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attacking without paying the tax is illegal")
    void cannotAttackWithoutPaying() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        addNonSickCreature(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Two Spheres tax {2} each — {4} total per attacker")
    void taxScalesWithEnchantmentCount() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        harness.addToBattlefield(player1, new SphereOfSafety());
        addNonSickCreature(player2, new GrizzlyBears());

        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        declareAttackers(player2, List.of(0));

        // Combat auto-resolves (no blockers), so only the paid tax is observable here.
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("The tax is charged per attacking creature")
    void taxIsPerAttacker() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        addNonSickCreature(player2, new GrizzlyBears());
        addNonSickCreature(player2, new GrizzlyBears());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Only the defending player's enchantments count")
    void countsOnlyDefendersEnchantments() {
        harness.addToBattlefield(player1, new SphereOfSafety());
        harness.addToBattlefield(player2, new SphereOfSafety());
        addNonSickCreature(player2, new GrizzlyBears());

        // player2's own Sphere doesn't raise the tax on player2's attackers.
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        // Bears sit at index 1 — player2's own Sphere occupies index 0.
        declareAttackers(player2, List.of(1));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    private void addNonSickCreature(Player player, Card card) {
        Permanent p = new Permanent(card);
        p.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(p);
    }
}
