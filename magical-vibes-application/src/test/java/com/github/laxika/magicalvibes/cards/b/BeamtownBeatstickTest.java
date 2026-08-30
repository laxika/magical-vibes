package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeamtownBeatstick.class, GrizzlyBears.class, InvasionOfInnistrad.class})
class BeamtownBeatstickTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and menace")
    void equippedCreatureGetsBoostAndMenace() {
        Permanent creature = addCreatureReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature dealing combat damage to a player creates a Treasure")
    void combatDamageToPlayerCreatesTreasure() {
        Permanent creature = addCreatureReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(treasuresFor(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Equipped creature dealing combat damage to a battle creates a Treasure")
    void combatDamageToBattleCreatesTreasure() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent creature = addCreatureReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(battle.getId());

        resolveCombat();

        assertThat(treasuresFor(player1)).hasSize(1);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage dealt only to a creature does not create a Treasure")
    void combatDamageToCreatureDoesNotCreateTreasure() {
        Permanent creature = addCreatureReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(treasuresFor(player1)).isEmpty();
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addEquipmentReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BeamtownBeatstick());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private List<Permanent> treasuresFor(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .toList();
    }
}
