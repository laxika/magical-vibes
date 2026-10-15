package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.g.GoblinBoarders;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BannerOfKinship.class, LlanowarElves.class, DwynensElite.class, GoblinBoarders.class})
class BannerOfKinshipTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a type gives Banner one fellowship counter per matching creature")
    void entersWithFellowshipCountersForChosenType() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new DwynensElite());
        harness.addToBattlefield(player1, new GoblinBoarders());

        harness.castFromHand(player1, new BannerOfKinship(), "{5}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        Permanent banner = findPermanent(player1, "Banner of Kinship");
        assertThat(banner.getCounterCount(CounterType.FELLOWSHIP)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chosen-type creatures get +1/+1 for each fellowship counter")
    void boostsChosenTypePerFellowshipCounter() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinBoarders());
        Permanent banner = harness.addToBattlefieldAndReturn(player1, new BannerOfKinship());
        banner.setChosenSubtype(CardSubtype.ELF);
        banner.setCounterCount(CounterType.FELLOWSHIP, 3);

        assertThat(gqs.computeStaticBonus(gd, elf).power()).isEqualTo(3);
        assertThat(gqs.computeStaticBonus(gd, elf).toughness()).isEqualTo(3);
        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(0);
        assertThat(gqs.computeStaticBonus(gd, goblin).toughness()).isEqualTo(0);
    }

    @Test
    void countsAndBoostsOnlyControllersCreatures() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.castFromHand(player1, new BannerOfKinship(), "{5}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        assertThat(findPermanent(player1, "Banner of Kinship").getCounterCount(CounterType.FELLOWSHIP))
                .isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, ownElf).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, ownElf).toughness()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, opposingElf).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, opposingElf).toughness()).isZero();
    }

    @Test
    void canChooseAbsentTypeAndEntersWithoutCounters() {
        harness.addToBattlefield(player1, new GoblinBoarders());
        harness.castFromHand(player1, new BannerOfKinship(), "{5}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        Permanent banner = findPermanent(player1, "Banner of Kinship");
        assertThat(banner.getCounterCount(CounterType.FELLOWSHIP)).isZero();

        harness.castFromHand(player1, new LlanowarElves(), "{G}");
        harness.passBothPriorities();
        Permanent elf = findPermanent(player1, "Llanowar Elves");
        assertThat(banner.getCounterCount(CounterType.FELLOWSHIP)).isZero();
        assertThat(gqs.computeStaticBonus(gd, elf).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, elf).toughness()).isZero();
    }

    @Test
    void laterCreaturesReceiveBonusWithoutIncreasingCounterCount() {
        harness.addToBattlefield(player1, new DwynensElite());
        harness.castFromHand(player1, new BannerOfKinship(), "{5}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");
        Permanent banner = findPermanent(player1, "Banner of Kinship");

        harness.castFromHand(player1, new LlanowarElves(), "{G}");
        harness.passBothPriorities();
        Permanent elf = findPermanent(player1, "Llanowar Elves");
        assertThat(banner.getCounterCount(CounterType.FELLOWSHIP)).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, elf).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, elf).toughness()).isEqualTo(1);

        banner.setCounterCount(CounterType.FELLOWSHIP, 4);
        banner.setCounterCount(CounterType.CHARGE, 2);
        assertThat(gqs.computeStaticBonus(gd, elf).power()).isEqualTo(4);
        assertThat(gqs.computeStaticBonus(gd, elf).toughness()).isEqualTo(4);

        banner.setCounterCount(CounterType.FELLOWSHIP, 0);
        assertThat(gqs.computeStaticBonus(gd, elf).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, elf).toughness()).isZero();
    }
}
