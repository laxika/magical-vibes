package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.cards.t.TaintedField;
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

@CardUsed({CripplingFatigue.class, SengirVampire.class, TaintedField.class})
class CripplingFatigueTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -2/-2 until end of turn")
    void givesTargetCreatureMinusTwoMinusTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.setHand(player1, List.of(new CripplingFatigue()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("-2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.setHand(player1, List.of(new CripplingFatigue()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Flashback pays 3 life and exiles Crippling Fatigue after resolving")
    void flashbackPaysLifeAndExiles() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        harness.setGraveyard(player1, List.of(new CripplingFatigue()));
        addFlashbackMana();

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        harness.assertNotInGraveyard(player1, "Crippling Fatigue");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Crippling Fatigue"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SengirVampire());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new TaintedField());
        harness.setHand(player1, List.of(new CripplingFatigue()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(target.getEffectivePower()).isEqualTo(4);
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
