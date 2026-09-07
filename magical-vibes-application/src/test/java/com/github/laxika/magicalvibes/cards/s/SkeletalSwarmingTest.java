package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ClatteringSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkeletalSwarming.class, ClatteringSkeletons.class, GrizzlyBears.class})
class SkeletalSwarmingTest extends BaseCardTest {

    @Test
    @DisplayName("Skeletons get trample and +1/+0 for each other Skeleton")
    void boostsSkeletonsByOtherSkeletons() {
        Permanent first = addCreatureReady(player1, new ClatteringSkeletons());
        Permanent second = addCreatureReady(player1, new ClatteringSkeletons());
        Permanent opponent = addCreatureReady(player2, new ClatteringSkeletons());
        int basePower = gqs.getEffectivePower(gd, first);
        harness.addToBattlefield(player1, new SkeletalSwarming());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only your Skeletons must attack each combat if able")
    void onlyOwnSkeletonsMustAttack() {
        harness.addToBattlefield(player1, new SkeletalSwarming());
        Permanent ownSkeleton = addCreatureReady(player1, new ClatteringSkeletons());
        addCreatureReady(player1, new GrizzlyBears());

        beginDeclareAttackers(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        addCreatureReady(player2, new ClatteringSkeletons());
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of());
        assertThat(ownSkeleton.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Creates one tapped Skeleton token when no creature died")
    void createsOneTappedSkeletonWithoutMorbid() {
        harness.addToBattlefield(player1, new SkeletalSwarming());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = skeletonTokens(player1);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates two tapped Skeleton tokens when a creature died")
    void createsTwoTappedSkeletonsWithMorbid() {
        harness.addToBattlefield(player1, new SkeletalSwarming());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = skeletonTokens(player1);
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allMatch(Permanent::isTapped);
    }

    private List<Permanent> skeletonTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Skeleton"))
                .toList();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
