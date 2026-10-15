package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SiegeOfTowers.class, Mountain.class, Forest.class})
class SiegeOfTowersTest extends BaseCardTest {

    @Test
    @DisplayName("Permanently makes a target Mountain a 3/1 creature that is still a land")
    void animatesMountainPermanently() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new SiegeOfTowers()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, mountain.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(1);
        assertThat(gqs.isLand(gd, mountain)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(1);
    }

    @Test
    @DisplayName("Replicate makes one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        castSiegeOfTowers(mountain, List.of("{1}{R}", "{1}{R}"));

        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(1);
    }

    @Test
    @DisplayName("Replicate copy may choose a new Mountain target")
    void replicateCopyMayTargetAnotherMountain() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player1, new Mountain());
        castSiegeOfTowers(originalTarget, List.of("{1}{R}"));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, originalTarget)).isTrue();
        assertThat(gqs.isCreature(gd, copyTarget)).isTrue();
        assertThat(gqs.getEffectivePower(gd, originalTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, originalTarget)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, copyTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copyTarget)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Mountain permanent")
    void cannotTargetNonMountain() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SiegeOfTowers()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mountain");
    }

    @Test
    @DisplayName("Can animate an opponent's Mountain without changing its controller")
    void animatesOpponentsMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        castSiegeOfTowers(mountain, List.of());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.isLand(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mountain);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mountain);
    }

    @Test
    @DisplayName("Without replicate payments only the original spell is put on the stack")
    void noCopiesWithoutReplicatePayment() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        castSiegeOfTowers(mountain, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCopy()).isFalse();
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A retargeted replicate copy resolves even if the original target leaves")
    void copyResolvesWhenOriginalTargetLeaves() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new Mountain());
        castSiegeOfTowers(originalTarget, List.of("{1}{R}"));
        gd.playerBattlefields.get(player1.getId()).remove(originalTarget);
        harness.setGraveyard(player1, List.of(originalTarget.getCard()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, copyTarget)).isTrue();
        assertThat(gqs.getEffectivePower(gd, copyTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copyTarget)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(originalTarget);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An animated Mountain retains its red mana ability and color")
    void animatedMountainRetainsManaAbilityAndColor() {
        Permanent mountain = addCreatureReady(player1, new Mountain());
        castSiegeOfTowers(mountain, List.of());
        resolveAllTriggers();

        harness.tapPermanent(player1, 0);

        assertThat(mountain.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, mountain)).isEmpty();
        assertThat(gqs.isLand(gd, mountain)).isTrue();
    }

    private void castSiegeOfTowers(Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new SiegeOfTowers()));
        harness.addMana(player1, ManaColor.COLORLESS, 1 + replicatePayments.size());
        harness.addMana(player1, ManaColor.RED, 1 + replicatePayments.size());
        harness.castSorceryWithRepeatedCosts(player1, 0, replicatePayments, List.of(target.getId()));
    }
}
