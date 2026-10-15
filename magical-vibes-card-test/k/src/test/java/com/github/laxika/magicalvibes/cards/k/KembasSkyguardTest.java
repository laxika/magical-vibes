package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GraspOfDarkness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KembasSkyguard.class, GraspOfDarkness.class})
class KembasSkyguardTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Kemba's Skyguard puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new KembasSkyguard(), "{1}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(KembasSkyguard.class);
    }

    @Test
    @DisplayName("Resolving puts Kemba's Skyguard on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castKembasSkyguard();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Kemba's Skyguard");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(KembasSkyguard.class);
    }

    @Test
    @DisplayName("ETB trigger causes controller to gain 2 life")
    void etbGainsLife() {
        castKembasSkyguard();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB gain life works with non-default life totals")
    void etbGainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);

        castKembasSkyguard();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castKembasSkyguard();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gain waits for the enter trigger to resolve")
    void lifeGainWaitsForTriggerResolution() {
        castKembasSkyguard();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Opponent's Skyguard gains life for its own controller")
    void opponentGainsLife() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new KembasSkyguard(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Enter trigger gains life even after Skyguard leaves the battlefield")
    void gainsLifeAfterSourceDies() {
        castKembasSkyguard();
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new GraspOfDarkness()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Kemba's Skyguard"));

        harness.assertInGraveyard(player1, "Kemba's Skyguard");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    private void castKembasSkyguard() {
        harness.castFromHand(player1, new KembasSkyguard(), "{1}{W}{W}");
    }
}
