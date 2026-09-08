package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeltstriderEulogist.class, GrizzlyBears.class, DoomBlade.class})
class MeltstriderEulogistTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a countered creature you control dies")
    void drawsWhenCounteredAllyCreatureDies() {
        addCreatureReady(player1, new MeltstriderEulogist());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        destroyCreature(player1, player1, creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when an uncountered creature you control dies")
    void doesNotDrawWhenAllyCreatureHasNoCounter() {
        addCreatureReady(player1, new MeltstriderEulogist());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        destroyCreature(player1, player1, creature);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when an opponent's countered creature dies")
    void doesNotDrawWhenOpponentCreatureDies() {
        addCreatureReady(player1, new MeltstriderEulogist());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        destroyCreature(player1, player2, creature);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void destroyCreature(Player caster, Player owner, Permanent creature) {
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInGraveyard(owner, "Grizzly Bears");
    }
}
