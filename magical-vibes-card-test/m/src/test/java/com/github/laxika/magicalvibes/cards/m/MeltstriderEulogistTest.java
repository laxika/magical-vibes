package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
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

@CardUsed({MeltstriderEulogist.class, GrizzlyBears.class, DoomBlade.class, DayOfJudgment.class})
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
        harness.castAndResolveInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(owner, "Grizzly Bears");
    }

    @Test
    void drawsWhenEulogistItselfDiesWithCounter() {
        Permanent eulogist = addCreatureReady(player1, new MeltstriderEulogist());
        eulogist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new MeltstriderEulogist()));
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, eulogist.getId());
        harness.assertInGraveyard(player1, "Meltstrider Eulogist");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsOnlyOnceForCreatureWithMultipleCounters() {
        addCreatureReady(player1, new MeltstriderEulogist());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        destroyCreature(player1, player1, creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void eachEulogistSeesBothCounteredCreaturesDieSimultaneously() {
        Permanent first = addCreatureReady(player1, new MeltstriderEulogist());
        Permanent second = addCreatureReady(player1, new MeltstriderEulogist());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new MeltstriderEulogist(), new MeltstriderEulogist(),
                new MeltstriderEulogist(), new MeltstriderEulogist()));
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }
}
