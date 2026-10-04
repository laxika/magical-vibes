package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ConsumingAetherborn;
import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeamtownBeatstick.class, ConsumingAetherborn.class, InvasionOfInnistrad.class, DelugeOfTheDead.class})
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

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passUntil(TurnStep.END_OF_COMBAT);

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

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passUntil(TurnStep.END_OF_COMBAT);

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
        Permanent secondBlocker = addCreatureReady(player2);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);

        resolveCombat();
        harness.withAutoStop(TurnStep.END_OF_COMBAT,
                () -> harness.handleCombatDamageAssigned(player1, 0,
                        Map.of(blocker.getId(), 2, secondBlocker.getId(), 1)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(treasuresFor(player1)).isEmpty();
    }

    @Test
    void equipAttachesForTwoMana() {
        Permanent equipment = addEquipmentReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    void equipRequiresTwoMana() {
        addEquipmentReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        addEquipmentReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        addEquipmentReady(player1);
        Permanent creature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unattachedEquipmentDoesNotBoostOrTrigger() {
        Permanent creature = addCreatureReady(player1);
        addEquipmentReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(treasuresFor(player1)).isEmpty();
    }

    @Test
    void treasureBelongsToEquipmentController() {
        Permanent creature = addCreatureReady(player1);
        Permanent equipment = addEquipmentReady(player2);
        equipment.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(treasuresFor(player1)).isEmpty();
        assertThat(treasuresFor(player2)).hasSize(1);
        assertThat(treasuresFor(player2).getFirst().isTapped()).isFalse();
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new ConsumingAetherborn());
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
