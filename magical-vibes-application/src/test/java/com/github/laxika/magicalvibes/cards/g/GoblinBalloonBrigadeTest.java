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

@CardUsed({GoblinBalloonBrigade.class})
class GoblinBalloonBrigadeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating flying ability puts it on the stack")
    void activatingFlyingPutsOnStack() {
        Permanent brigade = addCreatureReady(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
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
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefieldAndReturn(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability resolves without effect if Goblin Balloon Brigade leaves before resolution")
    void abilityResolvesWithoutEffectIfSourceRemoved() {
        addCreatureReady(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Nonred mana cannot pay for the flying ability")
    void cannotActivateWithOnlyNonredMana() {
        addCreatureReady(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying ability can be activated repeatedly and each activation costs red mana")
    void canActivateRepeatedly() {
        Permanent brigade = addCreatureReady(player1, new GoblinBalloonBrigade());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, brigade, Keyword.FLYING)).isTrue();
        assertThat(brigade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An activation does not grant flying to the same card after it reenters")
    void returningSourceIsANewPermanent() {
        GoblinBalloonBrigade card = new GoblinBalloonBrigade();
        Permanent original = addCreatureReady(player1, card);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
    }

}
