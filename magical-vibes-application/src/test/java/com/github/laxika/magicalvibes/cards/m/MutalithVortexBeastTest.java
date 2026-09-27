package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MutalithVortexBeast.class, Forest.class})
class MutalithVortexBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Flips once for the opponent and resolves the matching branch")
    void flipsForEachOpponentAndResolvesBranch() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new MutalithVortexBeast()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        long wins = gd.gameLog.stream()
                .filter(log -> log.plainText().contains("wins the coin flip for Mutalith Vortex Beast"))
                .count();
        long losses = gd.gameLog.stream()
                .filter(log -> log.plainText().contains("loses the coin flip for Mutalith Vortex Beast"))
                .count();

        assertThat(wins + losses).isEqualTo(1);
        if (wins == 1) {
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        } else {
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        }
    }
}
