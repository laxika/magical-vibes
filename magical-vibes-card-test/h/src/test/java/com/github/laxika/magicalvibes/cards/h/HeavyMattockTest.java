package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeavyMattock.class, HinterlandHermit.class, HinterlandScourge.class, DawntreaderElk.class})
class HeavyMattockTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped non-Human creature gets +1/+1")
    void equippedNonHumanGetsBaseBoost() {
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        Permanent mattock = addMattockReady(player1);
        mattock.setAttachedTo(creature.getId());

        // Dawntreader Elk 2/2 -> 3/3
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped Human creature gets an additional +1/+1 for +2/+2 total")
    void equippedHumanGetsAdditionalBoost() {
        Permanent human = addReadyHuman(player1);
        Permanent mattock = addMattockReady(player1);
        mattock.setAttachedTo(human.getId());

        // Hinterland Hermit 2/1 -> 4/3
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);
    }

    @Test
    @DisplayName("Moving Heavy Mattock from a Human to a non-Human removes the additional boost")
    void movingFromHumanToNonHumanRemovesAdditionalBoost() {
        Permanent mattock = addMattockReady(player1);
        Permanent human = addReadyHuman(player1);
        Permanent bear = addCreatureReady(player1, new DawntreaderElk());

        mattock.setAttachedTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);

        // Re-equip to the non-Human
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(mattock.getAttachedTo()).isEqualTo(bear.getId());
        // Human reverts to base, bear only gets +1/+1
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature loses boost when Heavy Mattock is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent human = addReadyHuman(player1);
        Permanent mattock = addMattockReady(player1);
        mattock.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(mattock);

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("Heavy Mattock does not affect creatures it is not attached to")
    void doesNotAffectOtherCreatures() {
        Permanent human = addReadyHuman(player1);
        Permanent other = addReadyHuman(player1);
        Permanent mattock = addMattockReady(player1);
        mattock.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating equip targets the creature and resolving attaches the Mattock")
    void equipAttachesToTarget() {
        Permanent mattock = addMattockReady(player1);
        Permanent human = addReadyHuman(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, human.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(human.getId());

        harness.passBothPriorities();

        assertThat(mattock.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated with only one mana")
    void equipRequiresTwoMana() {
        Permanent mattock = addMattockReady(player1);
        Permanent human = addReadyHuman(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, human.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mattock.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent mattock = addMattockReady(player1);
        Permanent human = addReadyHuman(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, human.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mattock.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        Permanent mattock = addMattockReady(player1);
        Permanent human = addReadyHuman(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, human.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mattock.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bonuses still apply when attached to an opponent's Human")
    void attachedOpponentsHumanGetsBothBonuses() {
        Permanent mattock = addMattockReady(player1);
        Permanent human = addReadyHuman(player2);
        mattock.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);
    }

    @Test
    @DisplayName("The additional bonus stops when the equipped Human transforms into a non-Human")
    void transformingEquippedHumanRemovesAdditionalBonus() {
        Permanent human = addReadyHuman(player1);
        Permanent mattock = addMattockReady(player1);
        mattock.setAttachedTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);

        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(human.isTransformed()).isTrue();
        assertThat(mattock.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);
    }

    private Permanent addMattockReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HeavyMattock());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyHuman(Player player) {
        return addCreatureReady(player, new HinterlandHermit());
    }
}
