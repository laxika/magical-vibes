package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({HazeOfRage.class, GrizzlyBears.class})
class HazeOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+0 until end of turn")
    void boostsOwnCreaturesUntilEndOfTurn() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HazeOfRage()));
        addMana(2);

        castHazeOfRage();
        resolveSpellAndStorm();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying buyback returns Haze of Rage to hand after it resolves")
    void buybackReturnsToHand() {
        harness.setHand(player1, List.of(new HazeOfRage()));
        addMana(4);

        harness.castSorceryWithBuyback(player1, 0, null);
        assertThat(gd.stack.stream().anyMatch(StackEntry::isBuyback)).isTrue();

        resolveSpellAndStorm();

        harness.assertInHand(player1, "Haze of Rage");
        harness.assertNotInGraveyard(player1, "Haze of Rage");
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Haze of Rage")
    void stormCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HazeOfRage()));
        addMana(2);

        castHazeOfRage();
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(5);
    }

    private void castHazeOfRage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);
    }

    private void resolveSpellAndStorm() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, amount - 1);
    }
}
