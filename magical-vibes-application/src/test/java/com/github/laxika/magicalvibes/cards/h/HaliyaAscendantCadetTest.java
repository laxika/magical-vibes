package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaliyaAscendantCadet.class, Forest.class, GrizzlyBears.class})
class HaliyaAscendantCadetTest extends BaseCardTest {

    @Test
    void entersAndAttacksWithTargetedCounterTriggers() {
        HaliyaAscendantCadet haliyaCard = new HaliyaAscendantCadet();
        harness.setHand(player1, List.of(haliyaCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent haliya = findPermanent(player1, "Haliya, Ascendant Cadet");
        harness.handlePermanentChosen(player1, haliya.getId());
        harness.passBothPriorities();

        assertThat(haliya.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, haliya.getId());
        harness.passBothPriorities();

        assertThat(haliya.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void drawsOnceWhenOneOrMoreCounteredCreaturesDealCombatDamage() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        addCreatureReady(player1, new HaliyaAscendantCadet());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        secondAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void creaturesWithoutPlusOneCountersDoNotTriggerTheDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        addCreatureReady(player1, new HaliyaAscendantCadet());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
