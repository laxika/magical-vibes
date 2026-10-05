package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlusTwoMace.class, SteadfastPaladin.class})
class PlusTwoMaceTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPlusTwo() {
        Permanent creature = addCreatureReady(player1, new SteadfastPaladin());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new PlusTwoMace());
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void boostAppliesOnlyWhileEquipped() {
        Permanent creature = addCreatureReady(player1, new SteadfastPaladin());
        Permanent otherCreature = addCreatureReady(player1, new SteadfastPaladin());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new PlusTwoMace());
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);

        mace.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void equipAbilityAttachesToYourCreatureForThreeMana() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new PlusTwoMace());
        Permanent creature = addCreatureReady(player1, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipConsumesThreeGenericMana() {
        harness.addToBattlefield(player1, new PlusTwoMace());
        Permanent creature = addCreatureReady(player1, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();
    }

    @Test
    void cannotEquipWithInsufficientMana() {
        harness.addToBattlefield(player1, new PlusTwoMace());
        Permanent creature = addCreatureReady(player1, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new PlusTwoMace());
        Permanent creature = addCreatureReady(player2, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipNoncreature() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new PlusTwoMace());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mace.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        harness.addToBattlefield(player1, new PlusTwoMace());
        Permanent creature = addCreatureReady(player1, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipWhileStackIsNonempty() {
        harness.addToBattlefield(player1, new PlusTwoMace());
        Permanent creature = addCreatureReady(player1, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("stack is empty");
        harness.passBothPriorities();
    }

    @Test
    void reEquipMovesBothBonusesOnlyOnResolution() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new PlusTwoMace());
        Permanent first = addCreatureReady(player1, new SteadfastPaladin());
        Permanent second = addCreatureReady(player1, new SteadfastPaladin());
        mace.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(mace.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    void failedReEquipKeepsOriginalAttachmentAndBonuses() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new PlusTwoMace());
        Permanent first = addCreatureReady(player1, new SteadfastPaladin());
        Permanent second = addCreatureReady(player1, new SteadfastPaladin());
        mace.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);

        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bonusEndsWhenMaceLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new SteadfastPaladin());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new PlusTwoMace());
        mace.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(mace);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
