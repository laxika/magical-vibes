package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfWundagore.class, BurstOfStrength.class, GrizzlyBears.class})
class KnightOfWundagoreTest extends BaseCardTest {

    @Test
    void putsACounterOnItselfWhenYouPutOneOnAnotherCreature() {
        Permanent knight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(player1, creature);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersOnlyOnceEachTurnAndNotForPuttingACounterOnItself() {
        Permanent knight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(player1, knight);
        resolveAllTriggers();
        putCounterOn(player1, firstCreature);
        resolveAllTriggers();
        putCounterOn(player1, secondCreature);
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersWhenYouPutACounterOnAnOpponentsCreature() {
        Permanent knight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        putCounterOn(player1, opponentCreature);
        resolveAllTriggers();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenAnOpponentPutsTheCounterOnYourCreature() {
        Permanent knight = addCreatureReady(player1, new KnightOfWundagore());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(player2, creature);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void putCounterOn(Player player, Permanent target) {
        harness.setHand(player, List.of(new BurstOfStrength()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castInstant(player, 0, target.getId());
        harness.passBothPriorities();
    }
}
