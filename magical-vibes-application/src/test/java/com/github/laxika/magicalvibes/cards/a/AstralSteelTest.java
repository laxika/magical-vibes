package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SilverKnight;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AstralSteel.class, SilverKnight.class, Stabilizer.class})
class AstralSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/+2 until end of turn")
    void boostsTargetCreature() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SilverKnight());
        castAstralSteel(knight);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Astral Steel")
    void stormCopiesForEachPriorSpell() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SilverKnight());
        gd.recordSpellCast(player1.getId(), new SilverKnight());
        gd.recordSpellCast(player2.getId(), new SilverKnight());

        castAstralSteel(knight);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(8);
    }

    @Test
    @DisplayName("Storm copies may choose new targets")
    void stormCopyMayChooseNewTarget() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new SilverKnight());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player1, new SilverKnight());
        gd.recordSpellCast(player1.getId(), new SilverKnight());

        castAstralSteel(originalTarget);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, originalTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, originalTarget)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, newTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, newTarget)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new SilverKnight());
        castAstralSteel(knight);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent stabilizer = harness.addToBattlefieldAndReturn(player1, new Stabilizer());
        harness.setHand(player1, List.of(new AstralSteel()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, stabilizer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castAstralSteel(Permanent target) {
        harness.setHand(player1, List.of(new AstralSteel()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
