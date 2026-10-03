package com.github.laxika.magicalvibes.cards.b;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladedSentinel.class})
class BladedSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bladed Sentinel puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BladedSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BladedSentinel.class);
    }

    @Test
    @DisplayName("Resolving puts Bladed Sentinel onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BladedSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bladed Sentinel");
    }

    @Test
    @DisplayName("Activating vigilance ability puts it on the stack")
    void activatingVigilancePutsOnStack() {
        Permanent sentinel = addCreatureReady(player1, new BladedSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(BladedSentinel.class);
        assertThat(entry.getTargetId()).isEqualTo(sentinel.getId());
    }

    @Test
    @DisplayName("Resolving vigilance ability grants vigilance until end of turn")
    void resolvingVigilanceAbilityGrantsVigilance() {
        Permanent sentinel = addCreatureReady(player1, new BladedSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance granted by ability resets at end of turn cleanup")
    void vigilanceResetsAtEndOfTurn() {
        Permanent sentinel = addCreatureReady(player1, new BladedSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap Bladed Sentinel")
    void activatingAbilityDoesNotTap() {
        Permanent sentinel = addCreatureReady(player1, new BladedSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without white mana")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new BladedSentinel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent sentinel = addCreatureReady(player1, new BladedSentinel());
        sentinel.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BladedSentinel.class);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BladedSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BladedSentinel.class);
    }

    @Test
    @DisplayName("Ability finishes without granting vigilance if its source has left the battlefield")
    void abilityHasNoEffectIfSourceRemoved() {
        addCreatureReady(player1, new BladedSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the activating Sentinel gains vigilance")
    void vigilanceAppliesOnlyToSource() {
        Permanent source = addCreatureReady(player1, new BladedSentinel());
        Permanent other = addCreatureReady(player1, new BladedSentinel());
        Permanent opponent = addCreatureReady(player2, new BladedSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance lets Bladed Sentinel attack without tapping")
    void grantedVigilancePreventsAttackTap() {
        Permanent sentinel = addCreatureReady(player1, new BladedSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(sentinel.isAttacking()).isTrue();
        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Granting vigilance to an already tapped creature does not untap it")
    void vigilanceDoesNotUntapSource() {
        Permanent sentinel = addCreatureReady(player1, new BladedSentinel());
        sentinel.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.VIGILANCE)).isTrue();
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A replacement Sentinel does not gain vigilance from the departed source's ability")
    void replacementSentinelDoesNotGainVigilance() {
        addCreatureReady(player1, new BladedSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent replacement = addCreatureReady(player1, new BladedSentinel());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.VIGILANCE)).isFalse();
    }
}
