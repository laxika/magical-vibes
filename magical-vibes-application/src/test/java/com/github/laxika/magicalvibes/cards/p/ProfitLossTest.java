package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Profit // Loss is one card whose two halves (and their fusion) are the three modes of a single
 * modal instant, each paying its own total cost.
 */
class ProfitLossTest extends BaseCardTest {

    private static final int PROFIT = 0;
    private static final int LOSS = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Profit pumps only your creatures +1/+1")
    void profitPumpsOwnCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProfitLoss()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, PROFIT, List.of());
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(3);
        assertThat(mine.getEffectiveToughness()).isEqualTo(3);
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Loss gives opponents' creatures -1/-1 and can kill a 1/1")
    void lossWeakensOpponentCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());

        harness.setHand(player1, List.of(new ProfitLoss()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, LOSS, List.of());
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Loss is castable off black mana alone — the mode's {2}{B} replaces the printed {1}{W}")
    void lossIsPaidWithItsOwnCost() {
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProfitLoss()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castModalInstant(player1, 0, LOSS, List.of());
        harness.passBothPriorities();

        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Fuse resolves Profit then Loss")
    void fuseResolvesBothHalves() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProfitLoss()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, FUSE, List.of());
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(3);
        assertThat(mine.getEffectiveToughness()).isEqualTo(3);
        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fuse cannot be cast for only one half's mana")
    void fuseRequiresBothHalvesCost() {
        harness.setHand(player1, List.of(new ProfitLoss()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boosts wear off at end of turn")
    void wearsOff() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ProfitLoss()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, FUSE, List.of());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(2);
        assertThat(mine.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }
}
