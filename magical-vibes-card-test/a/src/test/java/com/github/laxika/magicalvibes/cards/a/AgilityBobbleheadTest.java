package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgilityBobblehead.class, GrizzlyBears.class, RagingGoblin.class})
class AgilityBobbleheadTest extends BaseCardTest {

    @Test
    void manaAbilityAddsChosenColor() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new AgilityBobblehead());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bobblehead.isTapped()).isTrue();
    }

    @Test
    void targetsUpToTheNumberOfBobbleheadsYouControl() {
        harness.addToBattlefield(player1, new AgilityBobblehead());
        harness.addToBattlefield(player1, new AgilityBobblehead());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(second.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void rejectsMoreTargetsThanBobbleheadsControlled() {
        harness.addToBattlefield(player1, new AgilityBobblehead());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyHastyCreaturesCanBlockTargetThisTurn() {
        harness.addToBattlefield(player1, new AgilityBobblehead());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(attacker.getId()));
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(player1, List.of(1));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("haste");
    }

    @Test
    void hastyCreatureCanBlockTargetThisTurn() {
        harness.addToBattlefield(player1, new AgilityBobblehead());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new RagingGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(attacker.getId()));
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(player1, List.of(1));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canActivateWithNoTargets() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new AgilityBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(bobblehead.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new AgilityBobblehead());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new AgilityBobblehead());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(bobblehead.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsBobbleheadsDoNotIncreaseTargetLimit() {
        harness.addToBattlefield(player1, new AgilityBobblehead());
        harness.addToBattlefield(player2, new AgilityBobblehead());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removingBobbleheadsBeforeResolutionDoesNotReduceChosenTargets() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AgilityBobblehead());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AgilityBobblehead());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, other);
        });
        harness.passBothPriorities();

        assertThat(first.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(second.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(first.getBlockRestrictionsUntilEndOfTurn()).hasSize(1);
        assertThat(second.getBlockRestrictionsUntilEndOfTurn()).hasSize(1);
    }

    @Test
    void remainingLegalTargetStillReceivesBothEffects() {
        harness.addToBattlefield(player1, new AgilityBobblehead());
        harness.addToBattlefield(player1, new AgilityBobblehead());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));
        harness.passBothPriorities();

        assertThat(second.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(second.getBlockRestrictionsUntilEndOfTurn()).hasSize(1);
    }

    @Test
    void grantedHasteAllowsNewCreatureToAttack() {
        harness.addToBattlefield(player1, new AgilityBobblehead());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(attacker.getId()));
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    void bothEffectsExpireAfterTheTurn() {
        harness.addToBattlefield(player1, new AgilityBobblehead());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(creature.getBlockRestrictionsUntilEndOfTurn()).hasSize(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(creature.getBlockRestrictionsUntilEndOfTurn()).isEmpty();
    }
}
