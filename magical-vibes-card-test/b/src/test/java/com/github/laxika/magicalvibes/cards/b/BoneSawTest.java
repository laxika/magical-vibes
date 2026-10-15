package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.v.ValeronOutlander;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoneSaw.class, ValeronOutlander.class})
class BoneSawTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Bone Saw to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent boneSaw = addBoneSawReady(player1);
        Permanent creature = addCreatureReady(player1, new ValeronOutlander());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(boneSaw.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new ValeronOutlander());
        Permanent boneSaw = addBoneSawReady(player1);
        boneSaw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature loses boost when Bone Saw is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new ValeronOutlander());
        Permanent boneSaw = addBoneSawReady(player1);
        boneSaw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(boneSaw);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bone Saw does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new ValeronOutlander());
        Permanent otherCreature = addCreatureReady(player1, new ValeronOutlander());
        Permanent boneSaw = addBoneSawReady(player1);
        boneSaw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        addBoneSawReady(player1);
        Permanent creature = addCreatureReady(player2, new ValeronOutlander());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringOpponentTurn() {
        addBoneSawReady(player1);
        Permanent creature = addCreatureReady(player1, new ValeronOutlander());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        addBoneSawReady(player1);
        Permanent creature = addCreatureReady(player1, new ValeronOutlander());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void equipPaysOneGenericMana() {
        Permanent boneSaw = addBoneSawReady(player1);
        Permanent creature = addCreatureReady(player1, new ValeronOutlander());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(boneSaw.getAttachedTo()).isNull();
        harness.passBothPriorities();

        assertThat(boneSaw.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void reequippingMovesBoostOnlyOnResolution() {
        Permanent boneSaw = addBoneSawReady(player1);
        Permanent first = addCreatureReady(player1, new ValeronOutlander());
        Permanent second = addCreatureReady(player1, new ValeronOutlander());
        boneSaw.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(boneSaw.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(boneSaw.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    @Test
    void failedReequipKeepsExistingAttachment() {
        Permanent boneSaw = addBoneSawReady(player1);
        Permanent first = addCreatureReady(player1, new ValeronOutlander());
        Permanent second = addCreatureReady(player1, new ValeronOutlander());
        boneSaw.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(boneSaw.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipWhileStackIsNonempty() {
        addBoneSawReady(player1);
        Permanent creature = addCreatureReady(player1, new ValeronOutlander());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    private Permanent addBoneSawReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BoneSaw());
        perm.setSummoningSick(false);
        return perm;
    }
}
