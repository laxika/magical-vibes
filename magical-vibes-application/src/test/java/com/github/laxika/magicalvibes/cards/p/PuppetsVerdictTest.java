package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PuppetsVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures on the matching side of the coin flip")
    void destroysCreaturesFromMatchingPowerRange() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PuppetsVerdict()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        assertThat(logs).anyMatch(log -> log.contains("coin flip for Puppet's Verdict"));

        boolean wonFlip = logs.stream().anyMatch(log -> log.contains("wins the coin flip for Puppet's Verdict"));
        if (wonFlip) {
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertOnBattlefield(player1, "Hill Giant");
            harness.assertOnBattlefield(player2, "Hill Giant");
        } else {
            assertThat(logs).anyMatch(log -> log.contains("loses the coin flip for Puppet's Verdict"));
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Hill Giant");
            harness.assertNotOnBattlefield(player2, "Hill Giant");
        }
    }
}
