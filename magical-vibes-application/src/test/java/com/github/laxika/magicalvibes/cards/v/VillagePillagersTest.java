package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VillagePillagers.class, FountainOfYouth.class, GrizzlyBears.class})
class VillagePillagersTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield trigger deals wither damage only to opponents' creatures")
    void etbDamagesOpponentsCreaturesWithWither() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());

        castVillagePillagers();

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("A creature with any counter dying creates one tapped Treasure")
    void counteredOpponentCreatureCreatesTappedTreasure() {
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dyingCreature.setMarkedDamage(3);
        harness.addToBattlefield(player1, new VillagePillagers());

        harness.runStateBasedActions();
        resolveAllTriggers();

        List<Permanent> treasures = treasures(player1);
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature without a counter does not create a Treasure")
    void creatureWithoutCounterDoesNotCreateTreasure() {
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());
        dyingCreature.setMarkedDamage(2);
        harness.addToBattlefield(player1, new VillagePillagers());

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(treasures(player1)).isEmpty();
    }

    private void castVillagePillagers() {
        harness.setHand(player1, List.of(new VillagePillagers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> treasures(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .toList();
    }
}
