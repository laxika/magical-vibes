package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrawlersPlate.class, RuneclawBear.class})
class BrawlersPlateTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Brawler's Plate puts it on the battlefield unattached")
    void castingPutsOnBattlefieldUnattached() {
        harness.setHand(player1, List.of(new BrawlersPlate()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Brawler's Plate") && !p.isAttached());
    }

    @Test
    @DisplayName("Equip {4} attaches Brawler's Plate to target creature you control")
    void equipAttachesToCreature() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2 and has trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creatures are unaffected")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent other = addCreatureReady(player1, new RuneclawBear());
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses the boost and trample when Brawler's Plate leaves the battlefield")
    void bonusesLostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        plate.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(plate);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Equipped attacker tramples excess damage over its blocker")
    void trampleAssignsExcessDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        plate.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Brawler's Plate can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent creature1 = addCreatureReady(player1, new RuneclawBear());
        Permanent creature2 = addCreatureReady(player1, new RuneclawBear());
        plate.setAttachedTo(creature1.getId());

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void equipRequiresFourMana() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(plate.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(plate.isAttached()).isFalse();
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(plate.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    void cannotEquipNoncreature() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent otherPlate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherPlate.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(plate.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    void cannotEquipDuringOpponentsTurn() {
        harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipWhileStackIsNotEmpty() {
        harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        harness.passBothPriorities();
    }

    @Test
    void failedReEquipLeavesOriginalCreatureEquipped() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent original = addCreatureReady(player1, new RuneclawBear());
        Permanent target = addCreatureReady(player1, new RuneclawBear());
        plate.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(plate.getAttachedTo()).isEqualTo(original.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, original, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multiplePlatesStackTheirBonuses() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        first.setAttachedTo(creature.getId());
        second.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void attachedPlateStillBoostsCreatureAfterItsControllerChanges() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        plate.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.runStateBasedActions();

        assertThat(plate.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }
}
