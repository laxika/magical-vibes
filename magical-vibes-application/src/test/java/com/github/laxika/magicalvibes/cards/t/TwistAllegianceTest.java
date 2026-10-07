package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwistAllegiance.class, GrizzlyBears.class, Mountain.class})
class TwistAllegianceTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges, untaps, and gives haste to both players' creatures")
    void exchangesUntapsAndGivesHasteToBothSides() {
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
    @DisplayName("Does not exchange, untap, or give haste to noncreatures")
    void doesNotAffectNoncreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mine.tap();
        theirs.tap();

        castAndResolve();

        assertThat(controls(player1.getId(), mine.getId())).isTrue();
        assertThat(controls(player2.getId(), theirs.getId())).isTrue();
        assertThat(mine.isTapped()).isTrue();
        assertThat(theirs.isTapped()).isTrue();
        assertThat(mine.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(theirs.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Control and haste expire at end of turn")
    void controlAndHasteExpireAtEndOfTurn() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve();

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        });

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

    @Test
    @DisplayName("Transfers creatures even when the opponent has none")
    void transfersCreaturesToEmptyOpponentBattlefield() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        mine.tap();

        castAndResolve();

        assertThat(controls(player2.getId(), mine.getId())).isTrue();
        assertThat(mine.isTapped()).isFalse();
        assertThat(mine.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Gains creatures even when the caster has none")
    void gainsCreaturesWithEmptyCasterBattlefield() {
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        theirs.tap();

        castAndResolve();

        assertThat(controls(player1.getId(), theirs.getId())).isTrue();
        assertThat(theirs.isTapped()).isFalse();
        assertThat(theirs.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Transfers all creatures when the players have different counts")
    void transfersUnequalNumbersOfCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        mine.tap();
        first.tap();
        second.tap();

        castAndResolve();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(mine);
        for (Permanent creature : List.of(mine, first, second)) {
            assertThat(creature.isTapped()).isFalse();
            assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        }
    }

    @Test
    @CardUsed(Humility.class)
    @DisplayName("Both sides gain haste despite an earlier Humility")
    void grantsHasteAfterEarlierHumility() {
        harness.addToBattlefield(player1, new Humility());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, mine, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.HASTE)).isFalse();

        castAndResolve();

        assertThat(controls(player2.getId(), mine.getId())).isTrue();
        assertThat(controls(player1.getId(), theirs.getId())).isTrue();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mine, Keyword.HASTE)).isTrue();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new TwistAllegiance()));
        addMana();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.castAndResolveSorcery(player1, 0, player2.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private boolean controls(UUID playerId, UUID permanentId) {
        return gd.playerBattlefields.get(playerId).stream().anyMatch(p -> p.getId().equals(permanentId));
    }
}
