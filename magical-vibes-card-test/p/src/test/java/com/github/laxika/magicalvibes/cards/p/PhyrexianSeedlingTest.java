package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianSeedling.class, GrizzlyBears.class, PreyUpon.class})
class PhyrexianSeedlingTest extends BaseCardTest {

    @Test
    void entersWithOnePlusOneCounter() {
        harness.castFromHand(player1, new PhyrexianSeedling(), "{2}{G}");
        harness.passBothPriorities();

        Permanent seedling = findPermanent(player1, "Phyrexian Seedling");
        assertThat(seedling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(seedling.getEffectivePower()).isEqualTo(1);
        assertThat(seedling.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void combatDamageProliferatesThatManyTimes() {
        Permanent seedling = addCreatureReady(player1, new PhyrexianSeedling());
        seedling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        seedling.setAttacking(true);
        resolveCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void proliferatesBeforeLethalCombatDamageIsChecked() {
        Permanent seedling = addCreatureReady(player1, new PhyrexianSeedling());
        seedling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        seedling.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(seedling);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleMultiplePermanentsChosen(player1, List.of(seedling.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(seedling.getId()));

        assertThat(seedling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Phyrexian Seedling");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed({PhyrexianSeedling.class, GrizzlyBears.class, PreyUpon.class})
    void noncombatDamageAlsoProliferatesForEachDamageDealt() {
        Permanent seedling = addCreatureReady(player1, new PhyrexianSeedling());
        seedling.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(seedling.getId(), opponent.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        for (int i = 0; i < 3; i++) {
            harness.handleMultiplePermanentsChosen(player1, List.of(seedling.getId()));
        }

        assertThat(seedling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        harness.assertOnBattlefield(player1, "Phyrexian Seedling");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
