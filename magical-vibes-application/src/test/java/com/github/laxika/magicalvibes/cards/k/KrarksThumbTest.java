package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SorcerersStrongbox;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KrarksThumbTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces a logical coin flip with two flips and ignores one")
    void replacesCoinFlip() {
        harness.addToBattlefield(player1, new KrarksThumb());
        harness.addToBattlefield(player1, new SorcerersStrongbox());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        assertThat(logs).anyMatch(log -> log.contains(
                "coin flip for Sorcerer's Strongbox (flipped 2 coins and ignored 1)"));
    }
}
