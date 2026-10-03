package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AleshasVanguard.class})
class AleshasVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast does not grant haste or return the creature at end step")
    void normalCastDoesNotUseDash() {
        harness.setHand(player1, List.of(new AleshasVanguard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent vanguard = findPermanent(player1, "Alesha's Vanguard");
        assertThat(vanguard.hasKeyword(Keyword.HASTE)).isFalse();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Alesha's Vanguard")).isSameAs(vanguard);
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new AleshasVanguard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        Permanent vanguard = findPermanent(player1, "Alesha's Vanguard");
        assertThat(vanguard.hasKeyword(Keyword.HASTE)).isTrue();
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(findPermanent(player1, "Alesha's Vanguard")).isSameAs(vanguard);
        harness.assertNotInHand(player1, "Alesha's Vanguard");

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Alesha's Vanguard");
        harness.assertNotOnBattlefield(player1, "Alesha's Vanguard");
    }

    @Test
    @DisplayName("Resolving a dashed spell does not create an enter-the-battlefield trigger")
    void dashSchedulesReturnWithoutAnEtbTrigger() {
        harness.setHand(player1, List.of(new AleshasVanguard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Alesha's Vanguard");
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("Dash returns the creature only after its delayed end-step trigger resolves")
    void dashReturnUsesTheStackAtEndStep() {
        harness.setHand(player1, List.of(new AleshasVanguard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Alesha's Vanguard");
        harness.assertNotInHand(player1, "Alesha's Vanguard");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Alesha's Vanguard");
        harness.assertInHand(player1, "Alesha's Vanguard");
    }

    @Test
    @DisplayName("A dashed Vanguard can attack on the turn it enters")
    void dashedVanguardCanAttackImmediately() {
        harness.setHand(player1, List.of(new AleshasVanguard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
