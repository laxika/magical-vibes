package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.cards.c.CabalSurgeon;
import com.github.laxika.magicalvibes.cards.g.Gurzigost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WasteAway.class, AngelOfRetribution.class, CabalCoffers.class, CabalSurgeon.class, Gurzigost.class})
class WasteAwayTest extends BaseCardTest {

    @Test
    void discardsACardAndGivesTargetCreatureMinusFiveMinusFive() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gurzigost());
        harness.setHand(player1, List.of(new WasteAway(), new CabalSurgeon()));
        addMana();

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cabal Surgeon");
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void minusFiveMinusFiveWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gurzigost());
        harness.setHand(player1, List.of(new WasteAway(), new CabalSurgeon()));
        addMana();

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    void minusFiveMinusFiveKillsCreatureWithFiveToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        harness.setHand(player1, List.of(new WasteAway(), new CabalSurgeon()));
        addMana();

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angel of Retribution");
        harness.assertInGraveyard(player2, "Angel of Retribution");
    }

    @Test
    void cannotBeCastWithoutAnotherCardToDiscard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gurzigost());
        harness.setHand(player1, List.of(new WasteAway()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(8);
        harness.assertInHand(player1, "Waste Away");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CabalCoffers());
        harness.setHand(player1, List.of(new WasteAway(), new CabalSurgeon()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        harness.assertInHand(player1, "Waste Away");
        harness.assertInHand(player1, "Cabal Surgeon");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
