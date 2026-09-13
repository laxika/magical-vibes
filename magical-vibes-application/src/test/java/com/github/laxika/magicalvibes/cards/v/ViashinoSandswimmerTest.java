package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ViashinoSandswimmer.class)
class ViashinoSandswimmerTest extends BaseCardTest {

    @Test
    void coinFlipReturnsItToHandOrSacrificesIt() {
        addCreatureReady(player1, new ViashinoSandswimmer());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();

        boolean wonFlip = gameLogContains("wins the coin flip for Viashino Sandswimmer");

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        if (wonFlip) {
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(card -> card instanceof ViashinoSandswimmer);
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(card -> card instanceof ViashinoSandswimmer);
        } else {
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(card -> card instanceof ViashinoSandswimmer);
            assertThat(gd.playerHands.get(player1.getId()))
                    .noneMatch(card -> card instanceof ViashinoSandswimmer);
        }
    }
}
