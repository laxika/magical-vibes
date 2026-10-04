package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.r.RubblebackRhino;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChemistersTrick.class, DrudgeBeetle.class, RubblebackRhino.class})
class ChemistersTrickTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you don't control gets -2/-0 and must attack this turn")
    void shrinksAndForcesTarget() {
        Permanent target = addCreature(player2);
        Permanent own = addCreature(player1);
        harness.setHand(player1, List.of(new ChemistersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(target.getMustAttackTargetId()).isNull();
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(own.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The -2/-0 and must-attack wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new ChemistersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(target.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent own = addCreature(player1);
        addCreature(player2);
        harness.setHand(player1, List.of(new ChemistersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Overloaded, every creature you don't control gets -2/-0 and must attack")
    void overloadAffectsEveryCreatureYouDontControl() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        Permanent own = addCreature(player1);
        harness.setHand(player1, List.of(new ChemistersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(0);
        assertThat(first.isMustAttackThisTurn()).isTrue();
        assertThat(second.isMustAttackThisTurn()).isTrue();
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(own.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Overload cannot be paid with only the normal mana cost available")
    void overloadRequiresTheFullOverloadCost() {
        addCreature(player2);
        harness.setHand(player1, List.of(new ChemistersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void overloadAffectsHexproofCreaturesWithoutTargeting() {
        Permanent rhino = addCreatureReady(player2, new RubblebackRhino());
        harness.setHand(player1, List.of(new ChemistersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
        assertThat(rhino.isMustAttackThisTurn()).isTrue();
    }

    @Test
    void overloadDoesNotAffectCreaturesEnteringAfterResolution() {
        Permanent existing = addCreature(player2);
        harness.setHand(player1, List.of(new ChemistersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();
        Permanent newcomer = harness.enterBattlefieldAndReturn(player2, new DrudgeBeetle());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(newcomer.isMustAttackThisTurn()).isFalse();
    }

    @Test
    void overloadCanResolveWithoutOpposingCreatures() {
        Permanent own = addCreature(player1);
        harness.setHand(player1, List.of(new ChemistersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(own.isMustAttackThisTurn()).isFalse();
        harness.assertInGraveyard(player1, "Chemister's Trick");
    }

    @Test
    void affectedCreatureMustAttackWhenAble() {
        Permanent target = addCreature(player2);
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new ChemistersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        declareAttackers(player2, List.of(0));
    }

    @Test
    void tappedAffectedCreatureIsNotRequiredToAttack() {
        Permanent target = addCreature(player2);
        target.tap();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new ChemistersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(0);
        declareAttackers(player2, List.of());
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new DrudgeBeetle());
    }
}
