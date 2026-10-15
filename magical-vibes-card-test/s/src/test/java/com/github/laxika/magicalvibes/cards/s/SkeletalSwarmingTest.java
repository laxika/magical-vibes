package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ClatteringSkeletons;
import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SkeletalSwarming.class, ClatteringSkeletons.class, DireWolfProwler.class, PowerWordKill.class})
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
        addCreatureReady(player1, new DireWolfProwler());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        addCreatureReady(player2, new ClatteringSkeletons());
        declareAttackers(player2, List.of());
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

    @Test
    void loneSkeletonDoesNotCountItselfAndBoostUpdatesWhenTokensEnter() {
        Permanent skeleton = addCreatureReady(player1, new ClatteringSkeletons());
        Permanent wolf = addCreatureReady(player1, new DireWolfProwler());
        int basePower = gqs.getEffectivePower(gd, skeleton);
        int baseToughness = gqs.getEffectiveToughness(gd, skeleton);
        int wolfPower = gqs.getEffectivePower(gd, wolf);
        harness.addToBattlefield(player1, new SkeletalSwarming());

        assertThat(gqs.getEffectivePower(gd, skeleton)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, skeleton, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(wolfPower);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skeleton)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, skeleton)).isEqualTo(baseToughness);
        Permanent token = skeletonTokens(player1).getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void tappedAndSummoningSickSkeletonsAreNotRequiredToAttack() {
        harness.addToBattlefield(player1, new SkeletalSwarming());
        Permanent tapped = addCreatureReady(player1, new ClatteringSkeletons());
        tapped.tap();
        harness.addToBattlefield(player1, new ClatteringSkeletons());

        declareAttackers(player1, List.of());

        assertThat(findPermanents(player1, "Clattering Skeletons"))
                .allMatch(permanent -> !permanent.isAttacking());
    }

    @Test
    void doesNotCreateTokensDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new SkeletalSwarming());
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(skeletonTokens(player1)).isEmpty();
        assertThat(skeletonTokens(player2)).isEmpty();
    }

    @Test
    void creatureDyingInResponseUpgradesTokenCreation() {
        harness.addToBattlefield(player1, new SkeletalSwarming());
        Permanent victim = addCreatureReady(player2, new DireWolfProwler());
        harness.setHand(player1, List.of(new PowerWordKill()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        advanceToEndStep(player1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.passBothPriorities();

        assertThat(skeletonTokens(player1)).hasSize(2).allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player2, "Dire Wolf Prowler");
    }

    private List<Permanent> skeletonTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Skeleton"))
                .toList();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
