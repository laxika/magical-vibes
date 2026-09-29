package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JennyGeneratedAnomaly.class, Forest.class, GrizzlyBears.class})
class JennyGeneratedAnomalyTest extends BaseCardTest {

    @Test
    void doubleStrikeExploresTwiceWhenItDealsCombatDamage() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        Permanent jenny = addCreatureReady(player1, new JennyGeneratedAnomaly());
        jenny.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
    }

    @Test
    void exploringANonlandPutsACounterOnJenny() {
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));
        Permanent jenny = addCreatureReady(player1, new JennyGeneratedAnomaly());
        jenny.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(jenny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(nonland.getId());
    }
}
