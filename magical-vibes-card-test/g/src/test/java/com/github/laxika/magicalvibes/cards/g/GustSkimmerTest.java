package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GustSkimmer.class})
class GustSkimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Gust-Skimmer puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new GustSkimmer()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gust-Skimmer");
    }

    @Test
    @DisplayName("Resolving puts Gust-Skimmer onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new GustSkimmer()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gust-Skimmer");
    }

    @Test
    @DisplayName("Activating flying ability puts it on the stack")
    void activatingFlyingPutsOnStack() {
        Permanent skimmer = addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Gust-Skimmer");
        assertThat(entry.getTargetId()).isEqualTo(skimmer.getId());
    }

    @Test
    @DisplayName("Resolving flying ability grants flying until end of turn")
    void resolvingFlyingAbilityGrantsFlying() {
        Permanent skimmer = addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, skimmer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying granted by ability resets at end of turn cleanup")
    void flyingResetsAtEndOfTurn() {
        Permanent skimmer = addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, skimmer, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, skimmer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap Gust-Skimmer")
    void activatingAbilityDoesNotTap() {
        Permanent skimmer = addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(skimmer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without blue mana")
    void cannotActivateWithoutBlueMana() {
        addSkimmerReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent skimmer = addSkimmerReady(player1);
        skimmer.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gust-Skimmer");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new GustSkimmer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gust-Skimmer");
    }

    @Test
    @DisplayName("Ability has no effect if Gust-Skimmer is removed before resolution")
    void abilityHasNoEffectIfSourceRemoved() {
        addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying is granted only to the Gust-Skimmer whose ability was activated")
    void flyingIsGrantedOnlyToSource() {
        Permanent source = addSkimmerReady(player1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GustSkimmer());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GustSkimmer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying ability can be activated repeatedly and each activation costs blue mana")
    void repeatedActivationsEachRequireBlueMana() {
        Permanent skimmer = addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, skimmer, Keyword.FLYING)).isTrue();
        assertThat(skimmer.isTapped()).isFalse();
    }

    private Permanent addSkimmerReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GustSkimmer());
        perm.setSummoningSick(false);
        return perm;
    }
}
