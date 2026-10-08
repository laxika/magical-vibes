package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SomberwaldSpider.class, DeadWeight.class, DarkthicketWolf.class, MentorOfTheMeek.class})
class SomberwaldSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a 2/4 without morbid (no counters)")
    void entersWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SomberwaldSpider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent spider = findPermanent(player1, "Somberwald Spider");

        // No +1/+1 counters without morbid
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        // Entering with counters does not create a triggered ability.
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters with two +1/+1 counters when morbid is met (effectively 4/6)")
    void entersWithMorbidCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SomberwaldSpider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Simulate a creature having died this turn
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent spider = findPermanent(player1, "Somberwald Spider");

        assertThat(gd.stack).isEmpty();
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(spider.getEffectivePower()).isEqualTo(4);
        assertThat(spider.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Killing a creature with Dead Weight enables morbid for Somberwald Spider")
    void actualCreatureDeathEnablesMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DeadWeight(), new SomberwaldSpider()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());

        // Dead Weight reduces the Wolf to zero toughness.
        harness.castEnchantment(player1, 0, wolf.getId());
        harness.passBothPriorities();

        // The Wolf should be dead
        harness.assertNotOnBattlefield(player2, "Darkthicket Wolf");

        // Morbid is active after the creature dies.
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent spider = findPermanent(player1, "Somberwald Spider");

        assertThat(gd.stack).isEmpty();
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(spider.getEffectivePower()).isEqualTo(4);
        assertThat(spider.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Morbid counters prevent Mentor of the Meek from triggering")
    void entryTriggersSeeMorbidCounters() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        Permanent spider = harness.enterBattlefieldAndReturn(player1, new SomberwaldSpider());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Without morbid Mentor of the Meek triggers for the entering Spider")
    void entryTriggersSeeSpiderWithoutMorbid() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        Permanent spider = harness.enterBattlefieldAndReturn(player1, new SomberwaldSpider());

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple deaths of your own creatures still give exactly two counters")
    void multipleOwnCreatureDeathsGiveOnlyTwoCounters() {
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 3, Integer::sum);

        Permanent spider = harness.enterBattlefieldAndReturn(player1, new SomberwaldSpider());

        assertThat(gd.stack).isEmpty();
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
