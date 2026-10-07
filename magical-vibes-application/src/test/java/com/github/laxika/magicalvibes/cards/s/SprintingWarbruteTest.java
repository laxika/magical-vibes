package com.github.laxika.magicalvibes.cards.s;

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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SprintingWarbrute.class})
class SprintingWarbruteTest extends BaseCardTest {

    @Test
    @DisplayName("Sprinting Warbrute must attack when able")
    void mustAttackWhenAble() {
        Permanent warbrute = harness.addToBattlefieldAndReturn(player1, new SprintingWarbrute());
        warbrute.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A summoning-sick Warbrute is not required to attack")
    void summoningSickWarbruteMayStayBack() {
        harness.addToBattlefield(player1, new SprintingWarbrute());

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped Warbrute is not required to attack")
    void tappedWarbruteMayStayBack() {
        Permanent warbrute = harness.addToBattlefieldAndReturn(player1, new SprintingWarbrute());
        warbrute.setSummoningSick(false);
        warbrute.tap();

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Dash does not create an enters-the-battlefield trigger")
    void dashDoesNotCreateEtbTrigger() {
        harness.setHand(player1, List.of(new SprintingWarbrute()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sprinting Warbrute");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A dashed Warbrute must attack on the turn it enters")
    void dashedWarbruteMustAttackImmediately() {
        harness.setHand(player1, List.of(new SprintingWarbrute()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Dash return waits for its delayed end-step trigger to resolve")
    void dashReturnAllowsResponsesAtEndStep() {
        harness.setHand(player1, List.of(new SprintingWarbrute()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Sprinting Warbrute");
        harness.assertNotInHand(player1, "Sprinting Warbrute");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Sprinting Warbrute");
        harness.assertNotOnBattlefield(player1, "Sprinting Warbrute");
    }

    @Test
    @DisplayName("Normal cast does not grant haste or return the creature at end step")
    void normalCastDoesNotUseDash() {
        harness.setHand(player1, List.of(new SprintingWarbrute()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent warbrute = findPermanent(player1, "Sprinting Warbrute");
        assertThat(warbrute.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sprinting Warbrute")).isSameAs(warbrute);
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new SprintingWarbrute()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent warbrute = findPermanent(player1, "Sprinting Warbrute");
        assertThat(warbrute.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sprinting Warbrute");
        harness.assertNotOnBattlefield(player1, "Sprinting Warbrute");
    }
}
