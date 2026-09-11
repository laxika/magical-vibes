package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DauntingDefender;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkromasDevoted.class, DauntingDefender.class, GrizzlyBears.class})
class AkromasDevotedTest extends BaseCardTest {

    @Test
    void givesVigilanceToClericsYouControlIncludingItself() {
        harness.addToBattlefield(player1, new AkromasDevoted());
        harness.addToBattlefield(player1, new DauntingDefender());

        Permanent devoted = findPermanent(player1, "Akroma's Devoted");
        Permanent defender = findPermanent(player1, "Daunting Defender");

        assertThat(gqs.hasKeyword(gd, devoted, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, defender, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void doesNotGiveVigilanceToNonClericsOrOpponentsClerics() {
        harness.addToBattlefield(player1, new AkromasDevoted());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new DauntingDefender());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentCleric = findPermanent(player2, "Daunting Defender");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCleric, Keyword.VIGILANCE)).isFalse();
    }
}
