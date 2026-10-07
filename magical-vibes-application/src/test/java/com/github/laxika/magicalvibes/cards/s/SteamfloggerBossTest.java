package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteamfloggerBoss.class, NessianCourser.class})
class SteamfloggerBossTest extends BaseCardTest {

    @Test
    void boostsOtherRiggersYouControlWithHaste() {
        Permanent boss = harness.addToBattlefieldAndReturn(player1, new SteamfloggerBoss());
        int bossPowerBeforeAnotherBoss = gqs.getEffectivePower(gd, boss);
        assertThat(gqs.hasKeyword(gd, boss, Keyword.HASTE)).isFalse();

        Permanent nonRigger = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        int nonRiggerPower = gqs.getEffectivePower(gd, nonRigger);
        Permanent opposingBoss = harness.addToBattlefieldAndReturn(player2, new SteamfloggerBoss());
        int opposingBossPower = gqs.getEffectivePower(gd, opposingBoss);

        Permanent anotherBoss = harness.addToBattlefieldAndReturn(player1, new SteamfloggerBoss());

        assertThat(gqs.getEffectivePower(gd, boss)).isEqualTo(bossPowerBeforeAnotherBoss + 1);
        assertThat(gqs.hasKeyword(gd, boss, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, anotherBoss)).isEqualTo(bossPowerBeforeAnotherBoss + 1);
        assertThat(gqs.hasKeyword(gd, anotherBoss, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonRigger)).isEqualTo(nonRiggerPower);
        assertThat(gqs.hasKeyword(gd, nonRigger, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingBoss)).isEqualTo(opposingBossPower);
        assertThat(gqs.hasKeyword(gd, opposingBoss, Keyword.HASTE)).isFalse();
    }

    @Test
    void bonusesStackWithoutIncreasingToughness() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SteamfloggerBoss());
        int initialPower = gqs.getEffectivePower(gd, first);
        int initialToughness = gqs.getEffectiveToughness(gd, first);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SteamfloggerBoss());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new SteamfloggerBoss());

        for (Permanent boss : new Permanent[]{first, second, third}) {
            assertThat(gqs.getEffectivePower(gd, boss)).isEqualTo(initialPower + 2);
            assertThat(gqs.getEffectiveToughness(gd, boss)).isEqualTo(initialToughness);
            assertThat(gqs.hasKeyword(gd, boss, Keyword.HASTE)).isTrue();
        }
    }

    @Test
    void bonusAndHasteDisappearWhenOtherBossLeaves() {
        Permanent remainingBoss = harness.addToBattlefieldAndReturn(player1, new SteamfloggerBoss());
        int initialPower = gqs.getEffectivePower(gd, remainingBoss);
        Permanent departingBoss = harness.addToBattlefieldAndReturn(player1, new SteamfloggerBoss());
        assertThat(gqs.getEffectivePower(gd, remainingBoss)).isEqualTo(initialPower + 1);
        assertThat(gqs.hasKeyword(gd, remainingBoss, Keyword.HASTE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departingBoss));

        assertThat(gqs.getEffectivePower(gd, remainingBoss)).isEqualTo(initialPower);
        assertThat(gqs.hasKeyword(gd, remainingBoss, Keyword.HASTE)).isFalse();
    }
}
