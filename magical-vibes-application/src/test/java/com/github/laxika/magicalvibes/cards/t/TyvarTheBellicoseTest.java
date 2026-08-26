package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PriestOfTitania;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyvarTheBellicose.class, GrizzlyBears.class, LlanowarElves.class, PriestOfTitania.class})
class TyvarTheBellicoseTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Elves gain deathtouch until end of turn")
    void attackingElvesGainDeathtouch() {
        harness.addToBattlefield(player1, new TyvarTheBellicose());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Each creature gets counters from its first mana ability each turn")
    void manaAbilityResolutionPutsCountersOnEachSource() {
        harness.addToBattlefield(player1, new TyvarTheBellicose());
        Permanent priest = addCreatureReady(player1, new PriestOfTitania());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.tapPermanent(player1, 2);
        resolveAllTriggers();

        assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(elves.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
