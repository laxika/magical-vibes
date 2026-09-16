package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkromasDevoted.class, AvenRedeemer.class, AvenEnvoy.class})
class AkromasDevotedTest extends BaseCardTest {

    @Test
    void givesVigilanceToClericsYouControlIncludingItself() {
        Permanent devoted = harness.addToBattlefieldAndReturn(player1, new AkromasDevoted());
        Permanent redeemer = harness.addToBattlefieldAndReturn(player1, new AvenRedeemer());

        assertThat(gqs.hasKeyword(gd, devoted, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, redeemer, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void doesNotGiveVigilanceToNonClericsOrOpponentsClerics() {
        harness.addToBattlefield(player1, new AkromasDevoted());
        Permanent envoy = harness.addToBattlefieldAndReturn(player1, new AvenEnvoy());
        Permanent opponentCleric = harness.addToBattlefieldAndReturn(player2, new AvenRedeemer());

        assertThat(gqs.hasKeyword(gd, envoy, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCleric, Keyword.VIGILANCE)).isFalse();
    }
}
