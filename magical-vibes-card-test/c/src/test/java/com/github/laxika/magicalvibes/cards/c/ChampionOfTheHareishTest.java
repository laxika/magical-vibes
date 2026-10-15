package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShieldsOfVelisVel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionOfTheHareish.class, GrizzlyBears.class, ShieldsOfVelisVel.class, Conspiracy.class})
class ChampionOfTheHareishTest extends BaseCardTest {

    @Test
    void writesAnInitialBuddyAndAddsUnfamiliarTypes() {
        Permanent champion = harness.enterBattlefieldAndReturn(player1, new ChampionOfTheHareish());
        harness.handleListChoice(player1, "RABBIT");

        assertThat(gd.getBuddyList(player1.getId())).containsExactly(CardSubtype.RABBIT);

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gd.getBuddyList(player1.getId())).containsExactlyInAnyOrder(
                CardSubtype.RABBIT, CardSubtype.BEAR);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForItselfOrOpponentsCreatures() {
        Permanent champion = harness.enterBattlefieldAndReturn(player1, new ChampionOfTheHareish());
        harness.handleListChoice(player1, "SOLDIER");

        assertThat(gd.stack).isEmpty();
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getBuddyList(player1.getId())).containsExactly(CardSubtype.SOLDIER);
        assertThat(gd.getBuddyList(player2.getId())).isEmpty();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void sharesBuddiesBetweenChampionsWithoutAddingAlreadyListedTypes() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new ChampionOfTheHareish());
        harness.handleListChoice(player1, "RABBIT");
        Permanent second = harness.enterBattlefieldAndReturn(player1, new ChampionOfTheHareish());
        harness.handleListChoice(player1, "SOLDIER");
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent third = harness.enterBattlefieldAndReturn(player1, new ChampionOfTheHareish());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getBuddyList(player1.getId())).containsExactlyInAnyOrder(
                CardSubtype.RABBIT, CardSubtype.SOLDIER);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void usesCreatureTypesAtResolutionAfterTheyChangeInResponse() {
        Permanent champion = harness.enterBattlefieldAndReturn(player1, new ChampionOfTheHareish());
        harness.handleListChoice(player1, "RABBIT");
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShieldsOfVelisVel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getBuddyList(player1.getId())).containsExactly(CardSubtype.RABBIT);
    }

    @Test
    void initialBuddyChoiceUsesTypesChangedByConspiracy() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        harness.enterBattlefieldAndReturn(player1, new ChampionOfTheHareish());

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("ELF");
        harness.handleListChoice(player1, "ELF");
        assertThat(gd.getBuddyList(player1.getId())).containsExactly(CardSubtype.ELF);
    }
}
