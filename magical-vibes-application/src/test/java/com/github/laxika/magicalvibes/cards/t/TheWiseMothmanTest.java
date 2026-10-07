package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.r.RaulTroubleShooter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWiseMothman.class, Forest.class, GrizzlyBears.class, Millstone.class,
        LeylineOfTheVoid.class, RaulTroubleShooter.class})
class TheWiseMothmanTest extends BaseCardTest {

    @Test
    void entersGivingEachPlayerARadCounter() {
        harness.castFromHand(player1, new TheWiseMothman(), "{1}{B}{G}{U}");
        resolveAllTriggers();

        assertThat(gd.playerRadCounters).containsEntry(player1.getId(), 1);
        assertThat(gd.playerRadCounters).containsEntry(player2.getId(), 1);

    }

    @Test
    void attacksGivingEachPlayerARadCounter() {
        Permanent mothman = addCreatureReady(player1, new TheWiseMothman());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mothman)));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters).containsEntry(player1.getId(), 1);
        assertThat(gd.playerRadCounters).containsEntry(player2.getId(), 1);
    }

    @Test
    void putsCountersOnUpToTheNumberOfNonlandCardsMilled() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TheWiseMothman());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 3, null, player1.getId());
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void millingOnlyLandsDoesNotTrigger() {
        Permanent mothman = addCreatureReady(player1, new TheWiseMothman());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(mothman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentMillingMixedCardsCanPutCounterOnOpponentsCreature() {
        Permanent mothman = addCreatureReady(player1, new TheWiseMothman());
        Permanent opponent = addCreatureReady(player2, new RaulTroubleShooter());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player2, List.of(new Forest(), new TheWiseMothman()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(opponent.getId()));
        resolveAllTriggers();

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(mothman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayChooseNoCreaturesWhenNonlandsAreMilled() {
        Permanent mothman = addCreatureReady(player1, new TheWiseMothman());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new TheWiseMothman(), new RaulTroubleShooter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(mothman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void nonlandsMilledIntoExileStillTrigger() {
        Permanent mothman = addCreatureReady(player1, new TheWiseMothman());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        harness.setLibrary(player1, List.of(new TheWiseMothman(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(mothman.getId()));
        resolveAllTriggers();
        assertThat(mothman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void simultaneousMillAcrossPlayersTriggersOnceWithCombinedTargetLimit() {
        Permanent mothman = addCreatureReady(player1, new TheWiseMothman());
        Permanent raul = addCreatureReady(player1, new RaulTroubleShooter());
        harness.setLibrary(player1, List.of(new TheWiseMothman()));
        harness.setLibrary(player2, List.of(new RaulTroubleShooter()));

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(mothman.getId(), raul.getId()));
        resolveAllTriggers();

        assertThat(mothman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(raul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
