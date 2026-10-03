package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceArchitectOfThought;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvengingArrow.class, GrizzlyBears.class, ProdigalSorcerer.class, JaceArchitectOfThought.class})
class AvengingArrowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature that dealt combat damage to a player this turn")
    void destroysCreatureThatDealtCombatDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new AvengingArrow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that was only dealt damage itself")
    void cannotTargetCreatureThatOnlyTookDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setRegenerationShield(1);
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(bears.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(player1.getId());

        castArrow(bears);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }


    @Test
    @DisplayName("Destroys a creature that dealt noncombat damage to a player")
    void destroysCreatureThatDealtNoncombatDamageToPlayer() {
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.activateAbility(player2, indexOf(player2, sorcerer), null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        castArrow(sorcerer);
        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Destroys a creature that dealt damage only to a planeswalker")
    void destroysCreatureThatDamagedPlaneswalker() {
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceArchitectOfThought());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        harness.activateAbility(player2, indexOf(player2, sorcerer), null, jace.getId());
        harness.passBothPriorities();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        castArrow(sorcerer);
        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
        harness.assertOnBattlefield(player1, "Jace, Architect of Thought");
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage still qualifies after the damaged creature dies")
    void destroysCreatureAfterItsDamageVictimDies() {
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent victim = addCreatureReady(player1, new ProdigalSorcerer());
        harness.activateAbility(player2, indexOf(player2, sorcerer), null, victim.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Prodigal Sorcerer");
        castArrow(sorcerer);
        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
    }

    @Test
    @DisplayName("Can destroy your own creature after it dealt damage")
    void destroysOwnCreatureThatDealtDamage() {
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        harness.activateAbility(player1, indexOf(player1, sorcerer), null, player2.getId());
        harness.passBothPriorities();
        castArrow(sorcerer);
        harness.assertInGraveyard(player1, "Prodigal Sorcerer");
        harness.assertLife(player2, 19);
    }

    private void castArrow(Permanent target) {
        harness.setHand(player1, List.of(new AvengingArrow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
