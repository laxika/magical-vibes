package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarLoamspeaker;
import com.github.laxika.magicalvibes.cards.l.LootThePathfinder;
import com.github.laxika.magicalvibes.model.CounterType;
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

    @Test
    void doesNotGrantHasteToOpposingExhaustCreatures() {
        Permanent sala = harness.addToBattlefieldAndReturn(player1, new SalaDeckBoss());
        Permanent engineer = harness.addToBattlefieldAndReturn(player2, new SkystreakEngineer());

        assertThat(gqs.hasKeyword(gd, engineer, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sala, Keyword.HASTE)).isFalse();
    }

    @Test
    void usedExhaustAbilityStillGrantsHasteAndItsCopyAddsCounters() {
        harness.addToBattlefield(player1, new SalaDeckBoss());
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new SkystreakEngineer());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyExhaustManaAbilities() {
        harness.addToBattlefield(player1, new SalaDeckBoss());
        harness.addToBattlefield(player1, new LootThePathfinder());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyOpponentsExhaustAbilities() {
        harness.addToBattlefield(player1, new SalaDeckBoss());
        Permanent engineer = harness.addToBattlefieldAndReturn(player2, new SkystreakEngineer());
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanChooseANewTargetWithoutChangingTheOriginal() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SalaDeckBoss());
        harness.addToBattlefield(player1, new LootThePathfinder());
        Permanent engineer = harness.addToBattlefieldAndReturn(player2, new SkystreakEngineer());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, engineer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Skystreak Engineer");
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningNewTargetsStillCopiesTheExhaustAbility() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SalaDeckBoss());
        harness.addToBattlefield(player1, new LootThePathfinder());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }
}
