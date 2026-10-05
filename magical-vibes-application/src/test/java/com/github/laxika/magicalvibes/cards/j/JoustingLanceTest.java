package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.k.KrosanDruid;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoustingLance.class, KrosanDruid.class})
class JoustingLanceTest extends BaseCardTest {


    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        lance.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);    // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creature loses +2/+0 when lance is removed")
    void creatureLosesBoostWhenLanceRemoved() {
        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        lance.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(lance);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }


    @Test
    @DisplayName("Equipped creature has first strike during controller's turn")
    void equippedCreatureHasFirstStrikeDuringYourTurn() {
        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        lance.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature does NOT have first strike during opponent's turn")
    void equippedCreatureNoFirstStrikeDuringOpponentTurn() {
        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        lance.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike toggles when active player changes")
    void firstStrikeTogglesWithActivePlayer() {
        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        lance.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }


    @Test
    @DisplayName("+2/+0 boost applies during opponent's turn too")
    void boostAppliesDuringOpponentTurn() {
        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        lance.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);    // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }


    @Test
    @DisplayName("Moving lance transfers +2/+0 and conditional first strike to new creature")
    void movingLanceTransfersEffects() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        Permanent creature1 = addCreatureReady(player1, new KrosanDruid());
        Permanent creature2 = addCreatureReady(player1, new KrosanDruid());
        lance.setAttachedTo(creature1.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(lance.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.FIRST_STRIKE)).isTrue();
    }


    @Test
    @DisplayName("Equipped creature deals first strike combat damage on controller's turn")
    void equippedCreatureDealsFirstStrikeDamageOnYourTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        lance.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat(player1);

        // 2 base + 2 from lance = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }


    @Test
    @DisplayName("Equip costs three mana and leaves the creature unchanged until resolution")
    void equipResolvesAfterPayingThreeMana() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(lance.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(lance.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Two mana is insufficient to equip")
    void cannotEquipWithOnlyTwoMana() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(lance.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        Permanent creature = addCreatureReady(player2, new KrosanDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature artifact")
    void cannotEquipNoncreature() {
        harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        Permanent otherLance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherLance.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip is restricted to sorcery timing")
    void cannotEquipDuringCombat() {
        harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("First strike follows the Equipment controller's turn, not the creature controller's")
    void firstStrikeUsesEquipmentController() {
        Permanent creature = addCreatureReady(player2, new KrosanDruid());
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        lance.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal damage")
    void firstStrikePreventsBlockersDamage() {
        Permanent attacker = addCreatureReady(player1, new KrosanDruid());
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        lance.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new KrosanDruid());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(attacker.getId());

        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }
    @Test
    @DisplayName("Equip cannot be activated on an opponent's turn")
    void cannotEquipDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new JoustingLance());
        Permanent creature = addCreatureReady(player1, new KrosanDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("An equip target leaving does not detach the Lance from its previous creature")
    void equipFizzlesWithoutMovingLance() {
        Permanent lance = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        Permanent original = addCreatureReady(player1, new KrosanDruid());
        Permanent target = addCreatureReady(player1, new KrosanDruid());
        lance.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(lance.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}