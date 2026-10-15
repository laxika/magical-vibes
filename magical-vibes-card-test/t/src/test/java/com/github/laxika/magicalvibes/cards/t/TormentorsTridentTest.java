package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TormentorsTrident.class, MoorlandInquisitor.class})
class TormentorsTridentTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the equip ability attaches the Trident to the target creature")
    void equipAttachesToCreature() {
        Permanent trident = addTridentReady();
        Permanent creature = addCreatureReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(trident.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +3/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady();
        Permanent trident = addTridentReady();
        trident.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unequipped creatures are unaffected")
    void otherCreaturesUnaffected() {
        Permanent creature = addCreatureReady();
        Permanent other = addCreatureReady();
        Permanent trident = addTridentReady();
        trident.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature must attack each combat if able")
    void equippedCreatureMustAttack() {
        Permanent creature = addCreatureReady();
        Permanent trident = addTridentReady();
        trident.setAttachedTo(creature.getId());

        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A creature the Trident is not attached to is not forced to attack")
    void unequippedCreatureIsNotForcedToAttack() {
        Permanent creature = addCreatureReady();
        addTridentReady();

        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Equipped creature is not forced to attack while tapped")
    void tappedCreatureIsNotForcedToAttack() {
        Permanent creature = addCreatureReady();
        creature.tap();
        Permanent trident = addTridentReady();
        trident.setAttachedTo(creature.getId());

        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Equip costs {3}")
    void equipCostsThree() {
        addTridentReady();
        Permanent creature = addCreatureReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Summoning sickness prevents the equipped creature from being required to attack")
    void summoningSickCreatureIsNotForcedToAttack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        Permanent trident = addTridentReady();
        trident.setAttachedTo(creature.getId());

        beginDeclareAttackers();
        gs.declareAttackers(gd, player1, List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Re-equipping moves both the boost and attack requirement to the new creature")
    void reequippingMovesBothEffects() {
        Permanent trident = addTridentReady();
        Permanent original = addCreatureReady();
        Permanent replacement = addCreatureReady();
        trident.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(trident.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(2);

        beginDeclareAttackers();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(original))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
        gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(replacement)));

        assertThat(original.isAttacking()).isFalse();
        assertThat(replacement.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent trident = addTridentReady();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(trident.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        Permanent trident = addTridentReady();
        Permanent creature = addCreatureReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(trident.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An equip ability whose target leaves does not detach the original creature")
    void failedEquipKeepsOriginalAttachment() {
        Permanent trident = addTridentReady();
        Permanent original = addCreatureReady();
        Permanent target = addCreatureReady();
        trident.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(trident.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(5);
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addTridentReady() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new TormentorsTrident());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addCreatureReady() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        perm.setSummoningSick(false);
        return perm;
    }
}
