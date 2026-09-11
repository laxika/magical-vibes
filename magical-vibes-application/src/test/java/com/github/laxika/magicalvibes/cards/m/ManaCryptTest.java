package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ManaCrypt.class)
class ManaCryptTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mana Crypt produces two colorless mana")
    void tappingProducesTwoColorlessMana() {
        Permanent manaCrypt = harness.addToBattlefieldAndReturn(player1, new ManaCrypt());
        manaCrypt.setSummoningSick(false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(manaCrypt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At the beginning of your upkeep, Mana Crypt flips a coin and damages you on a loss")
    void upkeepCoinFlipDamagesControllerOnLoss() {
        harness.addToBattlefield(player1, new ManaCrypt());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Mana Crypt"));
        boolean lost = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("loses the coin flip for Mana Crypt"));

        assertThat(won).isNotEqualTo(lost);
        harness.assertLife(player1, lost ? 17 : 20);
    }
}
