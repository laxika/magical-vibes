package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonveilDragon.class, GrizzlyBears.class, Fling.class})
class MoonveilDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new MoonveilDragon());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Moonveil Dragon");
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
    }

    @Test
    @DisplayName("Resolving ability boosts each creature controlled by the activator")
    void resolvingBoostsEachOwnCreature() {
        Permanent dragon = addCreatureReady(player1, new MoonveilDragon());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated multiple times")
    void abilityStacksWithMultipleActivations() {
        Permanent dragon = addCreatureReady(player1, new MoonveilDragon());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent dragon = addCreatureReady(player1, new MoonveilDragon());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without red mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new MoonveilDragon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Tapped, summoning-sick Dragon can activate on an opponent's turn")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent dragon = harness.enterBattlefieldAndReturn(player1, new MoonveilDragon());
        dragon.setTapped(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(dragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost includes creatures entering before resolution but excludes later arrivals")
    void affectedCreaturesAreDeterminedAtResolution() {
        Permanent dragon = addCreatureReady(player1, new MoonveilDragon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new MoonveilDragon());
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(5);
        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new MoonveilDragon());

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activated boost resolves even after its source is sacrificed")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new MoonveilDragon());
        Permanent remainingDragon = addCreatureReady(player1, new MoonveilDragon());
        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), source.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, remainingDragon)).isEqualTo(5);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gqs.getEffectivePower(gd, remainingDragon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, remainingDragon)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana of another color cannot pay the red activation cost")
    void cannotActivateWithOnlyNonRedMana() {
        addCreatureReady(player1, new MoonveilDragon());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }
}
