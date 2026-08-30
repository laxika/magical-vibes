package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WasteAway.class, FountainOfYouth.class, Forest.class, MahamotiDjinn.class})
class WasteAwayTest extends BaseCardTest {

    @Test
    void discardsACardAndGivesTargetCreatureMinusFiveMinusFive() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MahamotiDjinn());
        harness.setHand(player1, List.of(new WasteAway(), new Forest()));
        addMana();

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(target.getEffectivePower()).isZero();
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void minusFiveMinusFiveWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MahamotiDjinn());
        harness.setHand(player1, List.of(new WasteAway(), new Forest()));
        addMana();

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    void cannotBeCastWithoutAnotherCardToDiscard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MahamotiDjinn());
        harness.setHand(player1, List.of(new WasteAway()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new WasteAway(), new Forest()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
