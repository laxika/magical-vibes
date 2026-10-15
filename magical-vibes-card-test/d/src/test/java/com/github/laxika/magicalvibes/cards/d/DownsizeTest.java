package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RubblebackRhino;
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

@CardUsed({Downsize.class, DrudgeBeetle.class, RubblebackRhino.class})
class DownsizeTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you don't control gets -4/-0")
    void shrinksTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new Downsize()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
    }

    @Test
    @DisplayName("The -4/-0 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new Downsize()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new Downsize()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Overloaded, every creature you don't control gets -4/-0 and no target is chosen")
    void overloadShrinksEveryCreatureYouDontControl() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new Downsize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(-2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(-2);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
    }

    @Test
    @DisplayName("Overload cannot be paid with only the normal mana cost available")
    void overloadRequiresTheFullOverloadCost() {
        harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new Downsize()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overload can resolve without any opposing creatures")
    void overloadWithNoOpposingCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new Downsize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof Downsize);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
    }

    @Test
    @DisplayName("Overload affects creatures present at resolution, but not creatures entering afterward")
    void overloadLocksInCreaturesAtResolution() {
        harness.setHand(player1, List.of(new Downsize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithOverload(player1, 0);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(2);
    }

    @Test
    @DisplayName("Overload affects hexproof creatures without targeting them")
    void overloadAffectsHexproofCreature() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player2, new RubblebackRhino());
        harness.setHand(player1, List.of(new Downsize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, rhino)).isEqualTo(4);
    }

    @Test
    @DisplayName("Normal casting cannot target an opposing hexproof creature")
    void normalCastingCannotTargetHexproofCreature() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player2, new RubblebackRhino());
        harness.setHand(player1, List.of(new Downsize()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, rhino.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectivePower(gd, rhino)).isEqualTo(3);
    }
}
