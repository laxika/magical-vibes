package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoroughBackup;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LukeCagePowerMan;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericaTeamLeader.class, BoroughBackup.class, LukeCagePowerMan.class,
        GrizzlyBears.class})
class CaptainAmericaTeamLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Another Hero entering gets counters and temporary vigilance and haste")
    void anotherHeroEnteringGetsCountersAndKeywords() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainAmericaTeamLeader());

        harness.setHand(player1, List.of(new LukeCagePowerMan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent luke = findPermanent(player1, "Luke Cage, Power Man");
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(luke.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, luke, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The temporary keywords expire but the counters remain")
    void keywordsExpireAtEndOfTurn() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainAmericaTeamLeader());

        harness.setHand(player1, List.of(new LukeCagePowerMan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent luke = findPermanent(player1, "Luke Cage, Power Man");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(luke.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, luke, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A non-Hero entering does not trigger Captain America")
    void nonHeroDoesNotTrigger() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainAmericaTeamLeader());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Hero token entering gets its own trigger")
    void eachHeroTokenTriggers() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainAmericaTeamLeader());

        harness.setHand(player1, List.of(new BoroughBackup()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Hero")).hasSize(2).allSatisfy(hero -> {
            assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, hero, Keyword.HASTE)).isTrue();
        });
    }
}
