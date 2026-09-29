package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuClimber.class, Forest.class})
class KavuClimberTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Kavu Climber puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new KavuClimber(), "{3}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.castFromHand(player1, new KavuClimber(), "{3}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kavu Climber");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.castFromHand(player1, new KavuClimber(), "{3}{G}{G}");

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }
}
