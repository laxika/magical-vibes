package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoggyNelsonOnRetainer.class, GrizzlyBears.class})
class FoggyNelsonOnRetainerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another creature and grants hexproof")
    void boostsAndProtectsAnotherCreature() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFoggyNelson();
        resolveEtbTargeting(ally);

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("ETB hexproof expires at the end of the turn")
    void grantedHexproofExpiresAtEndOfTurn() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFoggyNelson();
        resolveEtbTargeting(ally);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target Foggy Nelson itself or an opponent's creature")
    void rejectsIllegalTargets() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFoggyNelson();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(ally.getId());

        Permanent foggyNelson = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, foggyNelson.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void castFoggyNelson() {
        harness.setHand(player1, List.of(new FoggyNelsonOnRetainer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
