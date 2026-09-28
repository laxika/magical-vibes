package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MarcusMutantMayor.class, Forest.class, GrizzlyBears.class})
class MarcusMutantMayorTest extends BaseCardTest {

    @Test
    void creatureWithoutCounterGetsCounterInsteadOfDrawing() {
        harness.setHand(player1, List.of());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        addCreatureReady(player1, new MarcusMutantMayor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void creatureWithCounterDrawsInsteadOfGettingAnotherCounter() {
        harness.setHand(player1, List.of());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        addCreatureReady(player1, new MarcusMutantMayor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
