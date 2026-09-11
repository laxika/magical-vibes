package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamiyosSafekeeping.class, GrizzlyBears.class})
class TamiyosSafekeepingTest extends BaseCardTest {

    @Test
    @DisplayName("Target permanent gains hexproof and indestructible, and you gain 2 life")
    void protectsPermanentAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TamiyosSafekeeping()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Granted keywords expire at the end of the turn")
    void grantedKeywordsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TamiyosSafekeeping()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent")
    void cannotTargetOpponentsPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TamiyosSafekeeping()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }
}
