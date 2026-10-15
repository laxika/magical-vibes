package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HazeOfRage.class, NessianCourser.class})
class HazeOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+0 until end of turn")
    void boostsOwnCreaturesUntilEndOfTurn() {
        Permanent ownCourser = addCreatureReady(player1, new NessianCourser());
        Permanent opposingCourser = addCreatureReady(player2, new NessianCourser());
        harness.setHand(player1, List.of(new HazeOfRage()));
        addMana(2);

        castHazeOfRage();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownCourser)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCourser)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCourser)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Haze of Rage");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCourser)).isEqualTo(3);
    }

    @Test
    @DisplayName("Paying buyback returns Haze of Rage to hand after it resolves")
    void buybackReturnsToHand() {
        harness.setHand(player1, List.of(new HazeOfRage()));
        addMana(4);

        harness.castSorceryWithBuyback(player1, 0, null);
        assertThat(gd.stack.stream().anyMatch(StackEntry::isBuyback)).isTrue();

        resolveAllTriggers();

        harness.assertInHand(player1, "Haze of Rage");
        harness.assertNotInGraveyard(player1, "Haze of Rage");
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Haze of Rage")
    void stormCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new NessianCourser());
        gd.recordSpellCast(player2.getId(), new NessianCourser());
        Permanent ownCourser = addCreatureReady(player1, new NessianCourser());
        harness.setHand(player1, List.of(new HazeOfRage()));
        addMana(2);

        castHazeOfRage();
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownCourser)).isEqualTo(6);
    }

    @Test
    @DisplayName("Storm copies boost creatures but do not return extra cards with buyback")
    void stormWithBuybackReturnsOnlyOriginalCard() {
        gd.recordSpellCast(player2.getId(), new NessianCourser());
        Permanent courser = addCreatureReady(player1, new NessianCourser());
        harness.setHand(player1, List.of(new HazeOfRage()));
        addMana(4);

        harness.castSorceryWithBuyback(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, courser)).isEqualTo(4);
        harness.assertNotInHand(player1, "Haze of Rage");

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, courser)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Haze of Rage");
        harness.assertNotInGraveyard(player1, "Haze of Rage");
    }

    @Test
    @DisplayName("Recasting a bought-back Haze counts the original cast but not storm copies")
    void recastCountsSpellsRatherThanCopies() {
        Permanent courser = addCreatureReady(player1, new NessianCourser());
        harness.setHand(player1, List.of(new HazeOfRage()));
        addMana(4);
        harness.castSorceryWithBuyback(player1, 0, null);
        resolveAllTriggers();

        addMana(4);
        harness.castSorceryWithBuyback(player1, 0, null);
        resolveAllTriggers();

        addMana(2);
        castHazeOfRage();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, courser)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, courser)).isEqualTo(3);
        harness.assertNotInHand(player1, "Haze of Rage");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void boostDoesNotApplyToLaterCreatures() {
        harness.setHand(player1, List.of(new HazeOfRage()));
        addMana(2);
        castHazeOfRage();
        resolveAllTriggers();

        Permanent courser = addCreatureReady(player1, new NessianCourser());

        assertThat(gqs.getEffectivePower(gd, courser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, courser)).isEqualTo(3);
    }

    private void castHazeOfRage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);
    }

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, amount - 1);
    }
}
