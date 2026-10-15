package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AetherwingGoldenScaleFlagship;
import com.github.laxika.magicalvibes.cards.i.InvasionOfKaladesh;
import com.github.laxika.magicalvibes.cards.p.PacksongPup;
import com.github.laxika.magicalvibes.cards.r.RuneboundWolf;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChiefOfTheWilds.class, RuneboundWolf.class, GrizzlyBears.class, PacksongPup.class, Shock.class,
        InvasionOfKaladesh.class, AetherwingGoldenScaleFlagship.class, WrathOfGod.class})
class ChiefOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on itself when another Wolf enters")
    void putsCountersWhenAnotherWolfEnters() {
        Permanent chief = addCreatureReady(player1, new ChiefOfTheWilds());
        harness.setHand(player1, List.of(new RuneboundWolf()));
        harness.addMana(player1, ManaColor.RED, 10);
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(chief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when a non-Wolf creature enters")
    void doesNotTriggerForNonWolf() {
        Permanent chief = addCreatureReady(player1, new ChiefOfTheWilds());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(chief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Doubles triggered abilities of another Wolf")
    void doublesAnotherWolfsTriggeredAbility() {
        addCreatureReady(player1, new ChiefOfTheWilds());
        Permanent pup = addCreatureReady(player1, new PacksongPup());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, pup.getId());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new ChiefOfTheWilds()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Chief of the Wilds")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForAnOpponentsWolf() {
        Permanent chief = addCreatureReady(player1, new ChiefOfTheWilds());

        harness.enterBattlefieldAndReturn(player2, new RuneboundWolf());
        resolveAllTriggers();

        assertThat(chief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotDoubleAnOpponentsWolfAbility() {
        addCreatureReady(player1, new ChiefOfTheWilds());
        Permanent pup = addCreatureReady(player2, new PacksongPup());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, pup.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void doublesBattleEnterAbility() {
        Permanent chief = addCreatureReady(player1, new ChiefOfTheWilds());
        harness.setHand(player1, List.of(new InvasionOfKaladesh()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Thopter")).isEqualTo(2);
        assertThat(chief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void resolvingWolfAbilityTwiceUsesItsPowerAtDeath() {
        addCreatureReady(player1, new ChiefOfTheWilds());
        Permanent pup = addCreatureReady(player1, new PacksongPup());
        pup.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, pup.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    void doublesWolfDeathAbilityWhenChiefDiesSimultaneously() {
        addCreatureReady(player1, new ChiefOfTheWilds());
        addCreatureReady(player1, new PacksongPup());
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Chief of the Wilds");
        harness.assertInGraveyard(player1, "Packsong Pup");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }
}
