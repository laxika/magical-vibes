package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarnessByForce.class, GrizzlyBears.class, Forest.class})
class HarnessByForceTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps and grants haste to each targeted creature")
    void stealsUntapsAndGrantsHasteToEachTarget() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        first.tap();
        second.tap();
        castAndResolveHarnessByForce(List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(first.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(second.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castAndResolveHarnessByForce(List.of(target.getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(target.getId());
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Strive requires {2}{R} for each additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HarnessByForce()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only creature permanents can be targeted")
    void cannotTargetNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new HarnessByForce()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCastWithZeroTargetsForBaseCost() {
        Permanent untouched = addCreatureReady(player2, new GrizzlyBears());
        untouched.tap();
        harness.setHand(player1, List.of(new HarnessByForce()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(untouched.isTapped()).isTrue();
        assertThat(untouched.hasKeyword(Keyword.HASTE)).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Harness by Force");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();

        castAndResolveHarnessByForce(List.of(target.getId()));

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void paysStriveForEveryTargetBeyondFirst() {
        List<Permanent> targets = IntStream.range(0, 3)
                .mapToObj(i -> addCreatureReady(player2, new GrizzlyBears())).toList();
        harness.setHand(player1, List.of(new HarnessByForce()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, targets.stream().map(Permanent::getId).toList());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(targets);
        assertThat(targets).allSatisfy(target -> assertThat(target.hasKeyword(Keyword.HASTE)).isTrue());
    }

    @Test
    void stillAffectsRemainingTargetWhenAnotherLeavesBattlefield() {
        Permanent removed = addCreatureReady(player2, new GrizzlyBears());
        Permanent remaining = addCreatureReady(player2, new GrizzlyBears());
        remaining.tap();
        harness.setHand(player1, List.of(new HarnessByForce()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, List.of(removed.getId(), remaining.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removed);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remaining).doesNotContain(removed);
        assertThat(remaining.isTapped()).isFalse();
        assertThat(remaining.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canTargetMoreThanNinetyNineCreatures() {
        List<Permanent> targets = IntStream.range(0, 100)
                .mapToObj(i -> addCreatureReady(player2, new GrizzlyBears())).toList();
        harness.setHand(player1, List.of(new HarnessByForce()));
        harness.addMana(player1, ManaColor.RED, 101);
        harness.addMana(player1, ManaColor.COLORLESS, 199);

        harness.castAndResolveSorcery(player1, 0, targets.stream().map(Permanent::getId).toList());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(targets);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContainAnyElementsOf(targets);
        assertThat(targets).allSatisfy(target -> assertThat(target.hasKeyword(Keyword.HASTE)).isTrue());
    }

    private void castAndResolveHarnessByForce(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new HarnessByForce()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, targetIds.size() == 1 ? 1 : 3);
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }
}
