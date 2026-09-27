package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.t.TorrentOfStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WardOfPiety.class, Frostling.class, FrostOgre.class, TorrentOfStone.class,
        JaceBeleren.class, AwakenedSkyclave.class, InvasionOfZendikar.class})
class WardOfPietyTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage to the enchanted creature is redirected to the target creature")
    void redirectsDamageToCreature() {
        Permanent enchanted = addCreatureReady(player1, new FrostOgre());
        Permanent aura = attachWard(player1, enchanted);
        Permanent frostling = addCreatureReady(player1, new Frostling());
        Permanent destination = addCreatureReady(player2, new FrostOgre());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, frostling), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage can be redirected to a player")
    void redirectsDamageToPlayer() {
        Permanent enchanted = addCreatureReady(player1, new FrostOgre());
        Permanent aura = attachWard(player1, enchanted);
        Permanent frostling = addCreatureReady(player1, new Frostling());
        int lifeBefore = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, frostling), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Only the next 1 damage is redirected; the rest still lands on the enchanted creature")
    void redirectsOnlyOneDamage() {
        Permanent enchanted = addCreatureReady(player1, new FrostOgre());
        Permanent aura = attachWard(player1, enchanted);
        Permanent destination = addCreatureReady(player2, new FrostOgre());
        Permanent firstFrostling = addCreatureReady(player1, new Frostling());
        Permanent secondFrostling = addCreatureReady(player1, new Frostling());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, firstFrostling), null, enchanted.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondFrostling), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(enchanted.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only 1 damage from a larger damage event is redirected")
    void redirectsOnlyOneDamageFromSingleEvent() {
        Permanent enchanted = addCreatureReady(player1, new FrostOgre());
        Permanent aura = attachWard(player1, enchanted);
        Permanent destination = addCreatureReady(player2, new FrostOgre());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, aura), null, destination.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TorrentOfStone()));
        harness.castInstant(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(enchanted.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage can be redirected to a planeswalker")
    void redirectsDamageToPlaneswalker() {
        Permanent enchanted = addCreatureReady(player1, new FrostOgre());
        Permanent aura = attachWard(player1, enchanted);
        Permanent frostling = addCreatureReady(player1, new Frostling());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, planeswalker.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, frostling), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage can be redirected to a battle")
    void redirectsDamageToBattle() {
        Permanent enchanted = addCreatureReady(player1, new FrostOgre());
        Permanent aura = attachWard(player1, enchanted);
        Permanent frostling = addCreatureReady(player1, new Frostling());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, battle.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, frostling), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isZero();
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage can be redirected from a creature controlled by an opponent")
    void redirectsDamageFromOpponentsEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new FrostOgre());
        Permanent aura = attachWard(player1, enchanted);
        Permanent frostling = addCreatureReady(player2, new Frostling());
        Permanent destination = addCreatureReady(player1, new FrostOgre());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, indexOf(player2, frostling), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent enchanted = addCreatureReady(player1, new FrostOgre());
        Permanent aura = attachWard(player1, enchanted);
        Permanent destination = addCreatureReady(player2, new FrostOgre());
        Permanent frostling = addCreatureReady(player1, new Frostling());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, indexOf(player1, aura), null, destination.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, frostling), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(enchanted.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isZero();
    }

    private Permanent attachWard(Player player, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, new WardOfPiety());
        aura.setAttachedTo(enchanted.getId());
        aura.setSummoningSick(false);
        return aura;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
