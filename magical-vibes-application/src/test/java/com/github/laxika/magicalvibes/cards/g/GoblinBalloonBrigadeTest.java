package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GoblinBalloonBrigade.class)
class GoblinBalloonBrigadeTest extends BaseCardTest {

    // ===== Flying ability =====

    @Test
    @DisplayName("Activating flying ability puts it on the stack")
    void activatingFlyingPutsOnStack() {
        Permanent brigade = addCreatureReady(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Goblin Balloon Brigade");
        assertThat(entry.getTargetId()).isEqualTo(brigade.getId());
    }

    @Test
    @DisplayName("Resolving flying ability grants flying until end of turn")
    void resolvingFlyingAbilityGrantsFlying() {
        Permanent brigade = addCreatureReady(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying ability affects only its source creature")
    void flyingAbilityAffectsOnlySource() {
        Permanent brigade = addCreatureReady(player1, new GoblinBalloonBrigade());
        Permanent otherBrigade = addCreatureReady(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brigade, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherBrigade, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying granted by ability resets at end of turn cleanup")
    void flyingResetsAtEndOfTurn() {
        Permanent brigade = addCreatureReady(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brigade, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brigade, Keyword.FLYING)).isFalse();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Goblin Balloon Brigade")
    void activatingAbilityDoesNotTap() {
        Permanent brigade = addCreatureReady(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(brigade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new GoblinBalloonBrigade());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent brigade = addCreatureReady(player1, new GoblinBalloonBrigade());
        brigade.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Balloon Brigade");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent brigade = new Permanent(new GoblinBalloonBrigade());
        gd.playerBattlefields.get(player1.getId()).add(brigade);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Balloon Brigade");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Goblin Balloon Brigade is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addCreatureReady(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

}
