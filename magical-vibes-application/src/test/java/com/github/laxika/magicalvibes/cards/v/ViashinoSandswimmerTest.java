package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViashinoSandswimmerTest extends BaseCardTest {

    @Test
    void coinFlipReturnsItToHandOrSacrificesIt() {
        Permanent sandswimmer = new Permanent(new ViashinoSandswimmer());
        sandswimmer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sandswimmer);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        boolean wonFlip = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Viashino Sandswimmer"));

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
