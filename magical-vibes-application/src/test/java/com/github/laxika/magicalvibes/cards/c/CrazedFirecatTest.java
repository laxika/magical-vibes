package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CrazedFirecat.class)
class CrazedFirecatTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one +1/+1 counter on itself for each won flip before the first loss")
    void putsCounterForEachWonFlip() {
        harness.setHand(player1, List.of(new CrazedFirecat()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent firecat = findPermanent(player1, "Crazed Firecat");
        List<String> flipLogs = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Crazed Firecat"))
                .toList();
        long wins = flipLogs.stream().filter(log -> log.contains("wins the coin flip")).count();

        assertThat(flipLogs).isNotEmpty();
        assertThat(flipLogs.getLast()).contains("loses the coin flip");
        assertThat(firecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(wins);
    }
}
