package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TavernScoundrel.class, GrizzlyBears.class})
class TavernScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability sacrifices another permanent and creates Treasures when the flip is won")
    void sacrificesAnotherPermanentAndRewardsAWin() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent scoundrel = addCreatureReady(player1, new TavernScoundrel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Tavern Scoundrel"));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(scoundrel.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(won ? 2 : 0);
    }
}
