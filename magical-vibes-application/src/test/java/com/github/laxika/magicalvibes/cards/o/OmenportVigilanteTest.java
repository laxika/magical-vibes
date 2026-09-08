package com.github.laxika.magicalvibes.cards.o;

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

@CardUsed({OmenportVigilante.class, Shock.class})
class OmenportVigilanteTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have double strike before a crime")
    void noDoubleStrikeBeforeCrime() {
        Permanent vigilante = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());

        assertThat(gqs.hasKeyword(gd, vigilante, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gains double strike after targeting an opponent")
    void gainsDoubleStrikeAfterTargetingOpponent() {
        Permanent vigilante = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());
        castShockAt(player2.getId());

        assertThat(gqs.hasKeyword(gd, vigilante, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Targeting yourself does not count as a crime")
    void targetingYourselfDoesNotCommitCrime() {
        Permanent vigilante = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());
        castShockAt(player1.getId());

        assertThat(gqs.hasKeyword(gd, vigilante, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike wears off when the turn ends")
    void doubleStrikeWearsOffAtTurnEnd() {
        Permanent vigilante = harness.addToBattlefieldAndReturn(player1, new OmenportVigilante());
        castShockAt(player2.getId());
        assertThat(gqs.hasKeyword(gd, vigilante, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vigilante, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void castShockAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
