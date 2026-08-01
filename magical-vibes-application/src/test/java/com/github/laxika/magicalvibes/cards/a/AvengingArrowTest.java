package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvengingArrowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature that dealt combat damage to a player this turn")
    void destroysCreatureThatDealtCombatDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(bears.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());

        castArrow(bears);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a creature that dealt noncombat damage to a creature this turn")
    void destroysCreatureThatDealtNoncombatDamageToACreature() {
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player2, indexOf(player2, sorcerer), null, bears.getId());
        harness.passBothPriorities();

        castArrow(sorcerer);

        harness.assertNotOnBattlefield(player2, "Prodigal Sorcerer");
        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
    }

    @Test
    @DisplayName("Cannot target a creature that dealt no damage this turn")
    void cannotTargetCreatureThatDealtNoDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new AvengingArrow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that was only dealt damage itself")
    void cannotTargetCreatureThatOnlyTookDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        harness.setHand(player1, List.of(new AvengingArrow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A regeneration shield saves the creature — destruction is not regeneration-proof")
    void regenerationShieldSavesTheCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setRegenerationShield(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(bears.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());

        castArrow(bears);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castArrow(Permanent target) {
        harness.setHand(player1, List.of(new AvengingArrow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
