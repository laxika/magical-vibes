package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImprovisedArsenal.class, GrizzlyBears.class, Ornithopter.class})
class ImprovisedArsenalTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 for each artifact its controller controls")
    void equippedCreatureScalesWithControlledArtifacts() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent arsenal = harness.addToBattlefieldAndReturn(player1, new ImprovisedArsenal());
        arsenal.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.addToBattlefield(player1, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip attaches Improvised Arsenal to a creature you control")
    void equipAttachesToCreature() {
        Permanent arsenal = harness.addToBattlefieldAndReturn(player1, new ImprovisedArsenal());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(arsenal.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The activated ability creates a token copy of Improvised Arsenal")
    void createsTokenCopy() {
        harness.addToBattlefield(player1, new ImprovisedArsenal());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("A copy enters unattached and retains equip and the artifact-count bonus")
    void tokenCopyCanEquipAndStacksWithOriginal() {
        Permanent arsenal = harness.addToBattlefieldAndReturn(player1, new ImprovisedArsenal());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        arsenal.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(copy.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.addMana(player1, ManaColor.RED, 1);
        int copyIndex = gd.playerBattlefields.get(player1.getId()).indexOf(copy);
        harness.activateAbility(player1, copyIndex, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(copy.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A token copy can create another copy while tapped at instant speed")
    void tokenCopyRetainsCopyAbility() {
        harness.addToBattlefield(player1, new ImprovisedArsenal());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        copy.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(copy), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("The bonus counts the Equipment controller's artifacts even on an opposing creature")
    void bonusUsesEquipmentController() {
        Permanent arsenal = harness.addToBattlefieldAndReturn(player1, new ImprovisedArsenal());
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        arsenal.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpposingCreature() {
        harness.addToBattlefield(player1, new ImprovisedArsenal());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during the opponent's turn")
    void equipRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new ImprovisedArsenal());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.stack).isEmpty();
    }
}
