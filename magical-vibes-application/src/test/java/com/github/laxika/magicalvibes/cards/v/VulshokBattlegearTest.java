package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VulshokBattlegear.class, AlphaMyr.class})
class VulshokBattlegearTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent battlegear = addBattlegearReady(player1);
        battlegear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Unattached Battlegear does not boost creatures")
    void unattachedBattlegearDoesNotBoost() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        addBattlegearReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Battlegear removes its boost")
    void boostIsRemovedWhenBattlegearLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent battlegear = addBattlegearReady(player1);
        battlegear.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(battlegear);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip attaches Battlegear to a creature you control")
    void equipAttaches() {
        Permanent battlegear = addBattlegearReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(battlegear.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip requires three generic mana")
    void equipRequiresThreeMana() {
        Permanent battlegear = addBattlegearReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(battlegear.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip can be activated only at sorcery speed")
    void cannotEquipOutsideSorcerySpeed() {
        addBattlegearReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        Permanent battlegear = addBattlegearReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(battlegear.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip does nothing if the target leaves before resolution")
    void equipFizzlesIfTargetLeavesBeforeResolution() {
        Permanent battlegear = addBattlegearReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(battlegear.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Re-equipping moves the boost only when the ability resolves")
    void reEquipMovesBoostOnResolution() {
        Permanent battlegear = addBattlegearReady(player1);
        Permanent original = addCreatureReady(player1, new AlphaMyr());
        Permanent replacement = addCreatureReady(player1, new AlphaMyr());
        battlegear.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, replacement.getId());

        assertThat(battlegear.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(battlegear.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(4);
    }

    @Test
    @DisplayName("An illegal new equip target leaves the original attachment intact")
    void failedReEquipKeepsOriginalAttachment() {
        Permanent battlegear = addBattlegearReady(player1);
        Permanent original = addCreatureReady(player1, new AlphaMyr());
        Permanent replacement = addCreatureReady(player1, new AlphaMyr());
        battlegear.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, replacement.getId());
        gd.playerBattlefields.get(player1.getId()).remove(replacement);
        harness.passBothPriorities();

        assertThat(battlegear.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBattlegearReady(Player player) {
        return addCreatureReady(player, new VulshokBattlegear());
    }
}
