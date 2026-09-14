package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DartingMerfolk;
import com.github.laxika.magicalvibes.cards.i.IronLance;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FountainWatch.class, IronLance.class, IvoryMask.class, DartingMerfolk.class})
class FountainWatchTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts and enchantments you control have shroud")
    void artifactsAndEnchantmentsYouControlHaveShroud() {
        harness.addToBattlefield(player1, new FountainWatch());
        Permanent ironLance = harness.addToBattlefieldAndReturn(player1, new IronLance());
        Permanent ivoryMask = harness.addToBattlefieldAndReturn(player1, new IvoryMask());

        assertThat(gqs.hasKeyword(gd, ironLance, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, ivoryMask, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Creatures without artifact or enchantment types are unaffected")
    void otherPermanentsAreUnaffected() {
        harness.addToBattlefield(player1, new FountainWatch());
        Permanent dartingMerfolk = harness.addToBattlefieldAndReturn(player1, new DartingMerfolk());
        Permanent opponentIronLance = harness.addToBattlefieldAndReturn(player2, new IronLance());

        assertThat(gqs.hasKeyword(gd, dartingMerfolk, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentIronLance, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Granted shroud is lost when Fountain Watch leaves the battlefield")
    void shroudIsLostWhenFountainWatchLeaves() {
        Permanent fountainWatch = harness.addToBattlefieldAndReturn(player1, new FountainWatch());
        Permanent ironLance = harness.addToBattlefieldAndReturn(player1, new IronLance());

        assertThat(gqs.hasKeyword(gd, ironLance, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fountainWatch);

        assertThat(gqs.hasKeyword(gd, ironLance, Keyword.SHROUD)).isFalse();
    }
}
