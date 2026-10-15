package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GravestoneStrider;
import com.github.laxika.magicalvibes.cards.r.RecklessDetective;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThinkingCap.class, GravestoneStrider.class, RecklessDetective.class})
class ThinkingCapTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GravestoneStrider());
        Permanent cap = addReadyCap();
        cap.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equip Detective {1} attaches to a Detective")
    void equipDetectiveAttachesToDetective() {
        Permanent cap = addReadyCap();
        Permanent detective = addCreatureReady(player1, new RecklessDetective());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, detective.getId());
        harness.passBothPriorities();

        assertThat(cap.getAttachedTo()).isEqualTo(detective.getId());
    }

    @Test
    @DisplayName("Equip Detective {1} cannot target a non-Detective")
    void equipDetectiveCannotTargetNonDetective() {
        Permanent cap = addReadyCap();
        Permanent creature = addCreatureReady(player1, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cap.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {3} can attach to a non-Detective")
    void genericEquipAttachesToNonDetective() {
        Permanent cap = addReadyCap();
        Permanent creature = addCreatureReady(player1, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cap.getAttachedTo()).isEqualTo(creature.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither equip ability can target an opponent's Detective")
    void cannotEquipOpponentsDetective(int abilityIndex) {
        Permanent cap = addReadyCap();
        Permanent detective = addCreatureReady(player2, new RecklessDetective());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, detective.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cap.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both equip abilities require their full mana cost")
    void cannotEquipWithoutEnoughMana(int abilityIndex) {
        Permanent cap = addReadyCap();
        Permanent detective = addCreatureReady(player1, new RecklessDetective());
        if (abilityIndex == 1) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, detective.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cap.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither equip ability can be activated during combat")
    void equipRequiresMainPhase(int abilityIndex) {
        Permanent cap = addReadyCap();
        Permanent detective = addCreatureReady(player1, new RecklessDetective());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, detective.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cap.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither equip ability can be activated with an ability on the stack")
    void equipRequiresEmptyStack(int abilityIndex) {
        Permanent cap = addReadyCap();
        Permanent detective = addCreatureReady(player1, new RecklessDetective());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, detective.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, detective.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cap.getAttachedTo()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(cap.getAttachedTo()).isEqualTo(detective.getId());
    }

    @Test
    @DisplayName("Generic equip can move the cap from a Detective to a non-Detective")
    void reequippingMovesBoostToNewCreature() {
        Permanent cap = addReadyCap();
        Permanent detective = addCreatureReady(player1, new RecklessDetective());
        Permanent creature = addCreatureReady(player1, new GravestoneStrider());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, detective.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cap.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, detective)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Equip does not move the cap if its target changes controller before resolution")
    void targetMustStillBeControlledOnResolution(int abilityIndex) {
        Permanent cap = addReadyCap();
        Permanent original = addCreatureReady(player1, new GravestoneStrider());
        Permanent detective = addCreatureReady(player1, new RecklessDetective());
        cap.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, abilityIndex, null, detective.getId());

        gd.playerBattlefields.get(player1.getId()).remove(detective);
        gd.playerBattlefields.get(player2.getId()).add(detective);
        harness.passBothPriorities();

        assertThat(cap.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, detective)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCap() {
        return harness.addToBattlefieldAndReturn(player1, new ThinkingCap());
    }
}
