package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DivineCongregation.class, GrizzlyBears.class})
class DivineCongregationTest extends BaseCardTest {

    private void castDivineCongregation() {
        harness.setHand(player1, List.of(new DivineCongregation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains 2 life for each creature the target opponent controls")
    void gainsTwoLifePerCreature() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDivineCongregation();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Only the target opponent's creatures count")
    void ignoresCastersCreatures() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDivineCongregation();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains no life when the opponent controls no creatures")
    void gainsNothingWithoutCreatures() {
        harness.setLife(player1, 20);

        castDivineCongregation();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DivineCongregation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }
}
