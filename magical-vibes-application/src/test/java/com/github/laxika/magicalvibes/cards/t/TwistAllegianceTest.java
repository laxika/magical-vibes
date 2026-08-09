package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TwistAllegianceTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges, untaps, and gives haste to both players' creatures")
    void exchangesUntapsAndGivesHasteToBothSides() {
        enableAutoStop();
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        mine.tap();
        theirs.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        assertThat(mine.isTapped()).isTrue();
        assertThat(theirs.isTapped()).isTrue();

        castAndResolve();

        assertThat(controls(player2.getId(), mine.getId())).isTrue();
        assertThat(controls(player1.getId(), theirs.getId())).isTrue();
        assertThat(controls(player1.getId(), land.getId())).isTrue();
        assertThat(mine.isTapped()).isFalse();
        assertThat(theirs.isTapped()).isFalse();
        assertThat(mine.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(theirs.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at end of turn")
    void controlAndHasteExpireAtEndOfTurn() {
        enableAutoStop();
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(controls(player1.getId(), mine.getId())).isTrue();
        assertThat(controls(player2.getId(), theirs.getId())).isTrue();
        assertThat(mine.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(theirs.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void canTargetOnlyOpponent() {
        harness.setHand(player1, List.of(new TwistAllegiance()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new TwistAllegiance()));
        addMana();
        harness.castSorcery(player1, 0, player2.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void enableAutoStop() {
        Set<TurnStep> stops1 = ConcurrentHashMap.newKeySet();
        stops1.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops1);
        Set<TurnStep> stops2 = ConcurrentHashMap.newKeySet();
        stops2.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), stops2);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private boolean controls(UUID playerId, UUID permanentId) {
        return gd.playerBattlefields.get(playerId).stream().anyMatch(p -> p.getId().equals(permanentId));
    }
}
