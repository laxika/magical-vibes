package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarLoamspeaker;
import com.github.laxika.magicalvibes.cards.l.LootThePathfinder;
import com.github.laxika.magicalvibes.cards.s.SkystreakEngineer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SalaDeckBoss.class, SkystreakEngineer.class, LootThePathfinder.class,
        LlanowarLoamspeaker.class, GrizzlyBears.class, Forest.class})
class SalaDeckBossTest extends BaseCardTest {

    @Test
    void grantsHasteToCreaturesWithExhaustAbilities() {
        harness.addToBattlefield(player1, new SalaDeckBoss());
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new SkystreakEngineer());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, engineer, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
    }

    @Test
    void copiesTheNextExhaustAbility() {
        harness.addToBattlefield(player1, new SalaDeckBoss());
        Permanent loot = harness.addToBattlefieldAndReturn(player1, new LootThePathfinder());
        loot.setSummoningSick(false);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 6);
    }

    @Test
    void ignoresOtherNonManaAbilities() {
        harness.addToBattlefield(player1, new SalaDeckBoss());
        Permanent loamspeaker = harness.addToBattlefieldAndReturn(player1, new LlanowarLoamspeaker());
        loamspeaker.setSummoningSick(false);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 1, 1, null, forest.getId());

        assertThat(gd.stack).hasSize(1);
    }
}
