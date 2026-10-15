package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnduranceBobblehead.class, GrizzlyBears.class})
class EnduranceBobbleheadTest extends BaseCardTest {

    @Test
    void manaAbilityAddsChosenColor() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new EnduranceBobblehead());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bobblehead.isTapped()).isTrue();
    }

    @Test
    void targetsUpToTheNumberOfBobbleheadsAndGrantsPowerAndIndestructible() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(3);
        assertThat(first.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(second.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void rejectsMoreTargetsThanBobbleheadsControlled() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateProtectionDuringCombat() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateProtectionDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateProtectionWithAnAbilityOnTheStack() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(creature.getId()));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 1, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWithNoTargets() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new EnduranceBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(bobblehead.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsBobbleheadsDoNotIncreaseTargetLimit() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        harness.addToBattlefield(player2, new EnduranceBobblehead());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingBobbleheadsAfterActivationDoesNotReduceChosenTargets() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof EnduranceBobblehead);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(3);
        assertThat(first.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(second.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void powerAndIndestructibleExpireAtEndOfTurn() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void remainingTargetStillGetsBothEffectsWhenAnotherTargetLeaves() {
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        harness.addToBattlefield(player1, new EnduranceBobblehead());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);
        assertThat(second.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
