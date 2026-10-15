package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AeronautsWings.class, ArgothianSprite.class})
class AeronautsWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Aeronaut's Wings to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent wings = addWingsReady(player1);
        Permanent creature = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(wings.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+0 and flying")
    void equippedCreatureGetsBoostAndFlying() {
        Permanent creature = addCreatureReady(player1, new ArgothianSprite());
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creature loses the boost and flying when Aeronaut's Wings is removed")
    void creatureLosesEffectsWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new ArgothianSprite());
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(wings);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Aeronaut's Wings does not affect an unequipped creature")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new ArgothianSprite());
        Permanent otherCreature = addCreatureReady(player1, new ArgothianSprite());
        Permanent wings = addWingsReady(player1);
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    void reequippingMovesBothBenefitsAndPaysTwoMana() {
        Permanent wings = addWingsReady(player1);
        Permanent first = addCreatureReady(player1, new ArgothianSprite());
        Permanent second = addCreatureReady(player1, new ArgothianSprite());
        wings.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(wings.getAttachedTo()).isEqualTo(first.getId());
        harness.passBothPriorities();

        assertThat(wings.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    void failedReequipLeavesOriginalCreatureEquipped() {
        Permanent wings = addWingsReady(player1);
        Permanent first = addCreatureReady(player1, new ArgothianSprite());
        Permanent second = addCreatureReady(player1, new ArgothianSprite());
        wings.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(wings.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        addWingsReady(player1);
        Permanent creature = addCreatureReady(player2, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attachedEquipmentStillBenefitsCreatureAfterItsControllerChanges() {
        Permanent wings = addWingsReady(player1);
        Permanent creature = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        assertThat(wings.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void equipDoesNothingIfEquipmentLeavesBeforeResolution() {
        Permanent wings = addWingsReady(player1);
        Permanent creature = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(wings);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipNoncreature() {
        Permanent wings = addWingsReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wings.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(wings.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringOpponentsTurn() {
        addWingsReady(player1);
        Permanent creature = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        addWingsReady(player1);
        Permanent creature = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipWhileStackIsNonempty() {
        addWingsReady(player1);
        Permanent creature = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();
    }

    @Test
    void cannotEquipWithOnlyOneMana() {
        Permanent wings = addWingsReady(player1);
        Permanent creature = addCreatureReady(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wings.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private Permanent addWingsReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AeronautsWings());
        perm.setSummoningSick(false);
        return perm;
    }
}
