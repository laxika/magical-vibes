package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Twinferno.class, GrizzlyBears.class, Shock.class})
class TwinfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Copy mode copies the next instant or sorcery spell this turn")
    void copyModeCopiesNextInstantOrSorcery() {
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount)
                .containsEntry(player1.getId(), 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount)
                .doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Double-strike mode grants double strike to a creature you control")
    void doubleStrikeModeGrantsDoubleStrike() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double-strike mode cannot target an opponent's creature")
    void doubleStrikeModeCannotTargetOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");
    }
}
