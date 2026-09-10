package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriKeeperOfSongs.class, FountainOfYouth.class})
class OriKeeperOfSongsTest extends BaseCardTest {

    @Test
    void doesNotGetTheEnduringStoryBonusBeforeThreshold() {
        Permanent ori = harness.addToBattlefieldAndReturn(player1, new OriKeeperOfSongs());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, ori)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ori)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ori, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void getsBonusAndVigilanceAfterEnduringStoryAndKeepsThem() {
        Permanent ori = harness.addToBattlefieldAndReturn(player1, new OriKeeperOfSongs());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, ori)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ori)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ori, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Fountain of Youth"));

        assertThat(gqs.getEffectivePower(gd, ori)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ori, Keyword.VIGILANCE)).isTrue();
    }
}
