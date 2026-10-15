package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwordOfVengeance.class, RuneclawBear.class})
class SwordOfVengeanceTest extends BaseCardTest {

    

    

    @Test
    @DisplayName("Casting Sword of Vengeance and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new SwordOfVengeance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sword of Vengeance")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Sword to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped creature loses boost when Sword is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sword does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent otherCreature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has first strike")
    void equippedCreatureHasFirstStrike() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has vigilance")
    void equippedCreatureHasVigilance() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has trample")
    void equippedCreatureHasTrample() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has haste")
    void equippedCreatureHasHaste() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses all keywords when Sword is removed")
    void creatureLosesKeywordsWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(sword);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Sword can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        Permanent creature1 = addCreatureReady(player1, new RuneclawBear());
        Permanent creature2 = addCreatureReady(player1, new RuneclawBear());

        sword.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.HASTE)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.HASTE)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equip requires three mana")
    void equipRequiresThreeMana() {
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(sword.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        addCreatureReady(player1, new SwordOfVengeance());
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature")
    void cannotEquipNoncreature() {
        addCreatureReady(player1, new SwordOfVengeance());
        Permanent otherSword = addCreatureReady(player1, new SwordOfVengeance());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherSword.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void cannotEquipDuringUpkeep() {
        addCreatureReady(player1, new SwordOfVengeance());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Equip cannot be activated during the opponent's turn")
    void cannotEquipDuringOpponentTurn() {
        addCreatureReady(player1, new SwordOfVengeance());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Equip requires an empty stack")
    void cannotEquipWithNonemptyStack() {
        addCreatureReady(player1, new SwordOfVengeance());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A target leaving in response does not detach the original creature")
    void targetLeavingDoesNotDetachSword() {
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        Permanent original = addCreatureReady(player1, new RuneclawBear());
        Permanent target = addCreatureReady(player1, new RuneclawBear());
        sword.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        for (Keyword keyword : List.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.HASTE)) {
            assertThat(gqs.hasKeyword(gd, original, keyword)).isTrue();
        }
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bonuses remain when the equipped creature changes controller")
    void bonusesRemainAfterCreatureChangesController() {
        Permanent sword = addCreatureReady(player1, new SwordOfVengeance());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        sword.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        for (Keyword keyword : List.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.HASTE)) {
            assertThat(gqs.hasKeyword(gd, creature, keyword)).isTrue();
        }
    }

    @Test
    @DisplayName("Equipping a newly entered creature lets it attack without tapping")
    void hasteAndVigilanceAllowImmediateUntappedAttack() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfVengeance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }
}
