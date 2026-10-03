package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AuriokSteelshaper;
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

@CardUsed({BloodthirstyBlade.class, GrizzlyBears.class, AuriokSteelshaper.class})
class BloodthirstyBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and is goaded")
    void equippedCreatureGetsBoostAndGoaded() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BloodthirstyBlade());
        blade.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(als.getMustAttackRequirementCount(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip targets a creature an opponent controls")
    void equipAttachesToOpponentCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BloodthirstyBlade());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Equip cannot target a creature you control")
    void equipRejectsOwnCreature() {
        harness.addToBattlefield(player1, new BloodthirstyBlade());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attachmentCostIsNotReducedByEquipCostReduction() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BloodthirstyBlade());
        harness.addToBattlefield(player1, new AuriokSteelshaper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuriokSteelshaper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(blade.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void reattachingMovesBoostAndGoadToNewCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BloodthirstyBlade());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        blade.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.isGoaded(gd, first)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, first)).isZero();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.isGoaded(gd, second)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, second)).isEqualTo(1);
    }

    @Test
    void attachmentCannotBeActivatedOutsideMainPhase() {
        harness.addToBattlefield(player1, new BloodthirstyBlade());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attachmentCannotBeActivatedDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new BloodthirstyBlade());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void vanishedTargetLeavesBladeAttachedToOriginalCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BloodthirstyBlade());
        Permanent original = addCreatureReady(player2, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        blade.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.isGoaded(gd, original)).isTrue();
    }

    @Test
    void attachmentCannotBeActivatedWithAbilityOnStack() {
        harness.addToBattlefield(player1, new BloodthirstyBlade());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void goadedCreatureCannotDeclineToAttackInTwoPlayerGame() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BloodthirstyBlade());
        blade.setAttachedTo(target.getId());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        declareAttackers(player2, List.of(0));
        harness.assertLife(player1, 16);
    }

    @Test
    void tappedGoadedCreatureDoesNotHaveToAttack() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setTapped(true);
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BloodthirstyBlade());
        blade.setAttachedTo(target.getId());

        declareAttackers(player2, List.of());
        harness.assertLife(player1, 20);
    }
}
