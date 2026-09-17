package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CateranBrute;
import com.github.laxika.magicalvibes.cards.r.RishadanPort;
import com.github.laxika.magicalvibes.cards.r.RockBadger;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PuppetsVerdict.class, CateranBrute.class, RishadanPort.class, RockBadger.class})
class PuppetsVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures on the matching side of the coin flip")
    void destroysCreaturesFromMatchingPowerRange() {
        harness.addToBattlefield(player1, new CateranBrute());
        harness.addToBattlefield(player2, new CateranBrute());
        harness.addToBattlefield(player1, new RockBadger());
        harness.addToBattlefield(player2, new RockBadger());

        harness.castFromHand(player1, new PuppetsVerdict(), "{1}{R}{R}");
        harness.passBothPriorities();

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        assertThat(logs).anyMatch(log -> log.contains("coin flip for Puppet's Verdict"));

        boolean wonFlip = logs.stream().anyMatch(log -> log.contains("wins the coin flip for Puppet's Verdict"));
        if (wonFlip) {
            harness.assertNotOnBattlefield(player1, "Cateran Brute");
            harness.assertNotOnBattlefield(player2, "Cateran Brute");
            harness.assertOnBattlefield(player1, "Rock Badger");
            harness.assertOnBattlefield(player2, "Rock Badger");
        } else {
            assertThat(logs).anyMatch(log -> log.contains("loses the coin flip for Puppet's Verdict"));
            harness.assertOnBattlefield(player1, "Cateran Brute");
            harness.assertOnBattlefield(player2, "Cateran Brute");
            harness.assertNotOnBattlefield(player1, "Rock Badger");
            harness.assertNotOnBattlefield(player2, "Rock Badger");
        }
    }

    @Test
    @DisplayName("Does not destroy noncreature permanents")
    void doesNotDestroyNoncreaturePermanents() {
        harness.addToBattlefield(player1, new RishadanPort());
        harness.addToBattlefield(player2, new RishadanPort());

        harness.castFromHand(player1, new PuppetsVerdict(), "{1}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rishadan Port");
        harness.assertOnBattlefield(player2, "Rishadan Port");
    }
}
