package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.k.KolaghanAspirant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessImp.class, KolaghanAspirant.class})
class RecklessImpTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast does not grant haste or return the creature at end step")
    void normalCastDoesNotUseDash() {
        harness.setHand(player1, List.of(new RecklessImp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent imp = findPermanent(player1, "Reckless Imp");
        assertThat(imp.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Reckless Imp")).isSameAs(imp);
    }

    @Test
    @DisplayName("Dash grants haste and returns the creature to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new RecklessImp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent imp = findPermanent(player1, "Reckless Imp");
        assertThat(imp.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Reckless Imp");
        harness.assertNotOnBattlefield(player1, "Reckless Imp");
    }

    @Test
    @DisplayName("Reckless Imp cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent imp = addCreatureReady(player2, new RecklessImp());
        Permanent attacker = addCreatureReady(player1, new KolaghanAspirant());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(imp), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Resolving a dashed Imp does not create an enters-the-battlefield trigger")
    void dashDoesNotTriggerOnEntry() {
        harness.setHand(player1, List.of(new RecklessImp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reckless Imp");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dash creates exactly one return trigger at the next end step")
    void dashCreatesOnlyOneReturnTrigger() {
        harness.setHand(player1, List.of(new RecklessImp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Reckless Imp");
        harness.assertNotInHand(player1, "Reckless Imp");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertInHand(player1, "Reckless Imp");
        harness.assertNotOnBattlefield(player1, "Reckless Imp");
    }
}
