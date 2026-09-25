package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoveOnTheBattlefield.class, GrizzlyBears.class})
class LoveOnTheBattlefieldTest extends BaseCardTest {

    @Test
    @DisplayName("Exactly two attackers gain first strike and draw one card")
    void exactlyTwoAttackersGainFirstStrikeAndDraw() {
        harness.addToBattlefield(player1, new LoveOnTheBattlefield());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, firstAttacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondAttacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("A different number of attackers does not trigger")
    void differentAttackerCountDoesNotTrigger() {
        harness.addToBattlefield(player1, new LoveOnTheBattlefield());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card notDrawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(notDrawn));

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, firstAttacker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(notDrawn);
    }

    @Test
    @DisplayName("Each of the two attackers gets a counter when it deals combat damage")
    void combatDamagePutsCounterOnTheDamageDealer() {
        harness.addToBattlefield(player1, new LoveOnTheBattlefield());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(firstAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
