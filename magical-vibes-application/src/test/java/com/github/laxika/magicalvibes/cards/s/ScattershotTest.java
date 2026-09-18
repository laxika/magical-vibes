package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinPsychopath;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scattershot.class, GoblinPsychopath.class, Stabilizer.class})
class ScattershotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void dealsDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinPsychopath());

        castScattershot(target);
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).isEmpty();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Scattershot")
    void stormCreatesCopiesForEachPriorSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinPsychopath());
        gd.recordSpellCast(player1.getId(), new GoblinPsychopath());
        gd.recordSpellCast(player2.getId(), new GoblinPsychopath());

        castScattershot(target);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Storm copies may choose new creature targets")
    void stormCopyMayChooseNewTarget() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GoblinPsychopath());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new GoblinPsychopath());
        gd.recordSpellCast(player1.getId(), new GoblinPsychopath());

        castScattershot(originalTarget);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        resolveAllTriggers();

        assertThat(originalTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(newTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Stabilizer());
        harness.setHand(player1, List.of(new Scattershot()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castScattershot(Permanent target) {
        harness.setHand(player1, List.of(new Scattershot()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
