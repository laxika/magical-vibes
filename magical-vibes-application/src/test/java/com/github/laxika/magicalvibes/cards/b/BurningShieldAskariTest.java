package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurningShieldAskari.class, FemerefScouts.class, ZhalfirinKnight.class})
class BurningShieldAskariTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts it on the stack")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new BurningShieldAskari());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability grants first strike")
    void resolvingGrantsFirstStrike() {
        Permanent askari = addCreatureReady(player1, new BurningShieldAskari());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThat(gqs.hasKeyword(gd, askari, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, askari, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent askari = addCreatureReady(player1, new BurningShieldAskari());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, askari, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough red mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new BurningShieldAskari());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability does not tap the creature and works while summoning sick")
    void activatingNeedsNoTapOrHaste() {
        Permanent askari = harness.addToBattlefieldAndReturn(player1, new BurningShieldAskari());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(askari.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingShrinksNonFlankingBlocker() {
        Permanent askari = addCreatureReady(player1, new BurningShieldAskari());
        askari.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Flanking does not affect a blocker that also has flanking")
    void flankingDoesNotShrinkFlankingBlocker() {
        Permanent askari = addCreatureReady(player1, new BurningShieldAskari());
        askari.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZhalfirinKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An unblocked creature with flanking creates no trigger")
    void unblockedCreatesNoTrigger() {
        Permanent askari = addCreatureReady(player1, new BurningShieldAskari());
        askari.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each non-flanking blocker gets its own flanking penalty")
    void eachNonFlankingBlockerIsShrunk() {
        Permanent askari = addCreatureReady(player1, new BurningShieldAskari());
        askari.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new FemerefScouts());
        Permanent secondBlocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(firstBlocker.getEffectivePower()).isZero();
        assertThat(firstBlocker.getEffectiveToughness()).isEqualTo(3);
        assertThat(secondBlocker.getEffectivePower()).isZero();
        assertThat(secondBlocker.getEffectiveToughness()).isEqualTo(3);
    }
}
