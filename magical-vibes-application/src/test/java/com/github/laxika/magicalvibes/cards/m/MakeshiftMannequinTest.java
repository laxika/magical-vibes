package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MakeshiftMannequin.class, GrizzlyBears.class, ProdigalPyromancer.class, Shock.class,
        DoublingSeason.class, Lignify.class})
class MakeshiftMannequinTest extends BaseCardTest {

    private void addCost() {
        harness.addMana(player1, ManaColor.BLACK, 4);
    }

    private Permanent reanimate(Card creature) {
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature)));
        harness.setHand(player1, List.of(new MakeshiftMannequin()));
        addCost();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        return findPermanent(player1, creature.getName());
    }

    @Test
    @DisplayName("Returns target creature card from graveyard to battlefield with a mannequin counter")
    void reanimatesWithMannequinCounter() {
        Permanent bears = reanimate(new GrizzlyBears());

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.MANNEQUIN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reanimated creature is sacrificed when targeted by a spell")
    void sacrificedWhenTargetedBySpell() {
        Permanent bears = reanimate(new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());

        // Shock + the mannequin sacrifice trigger (on top) are on the stack
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Reanimated creature is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent bears = reanimate(new GrizzlyBears());

        Permanent pyro = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyro),
                null, bears.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
        harness.setHand(player1, List.of(new MakeshiftMannequin()));
        addCost();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new MakeshiftMannequin()));
        addCost();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doublingSeasonDoublesEnteringMannequinCounter() {
        harness.addToBattlefield(player1, new DoublingSeason());

        Permanent bears = reanimate(new GrizzlyBears());

        assertThat(bears.getCounterCount(CounterType.MANNEQUIN)).isEqualTo(2);
    }

    @Test
    void mannequinCounterOnAnotherCreatureDoesNotGrantAbility() {
        Permanent returned = reanimate(new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        returned.setCounterCount(CounterType.MANNEQUIN, 0);
        other.setCounterCount(CounterType.MANNEQUIN, 1);
        Permanent pyro = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyro),
                null, other.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other);
        assertThat(other.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void restoringCounterDoesNotRestartExpiredGrant() {
        Permanent bears = reanimate(new GrizzlyBears());
        bears.setCounterCount(CounterType.MANNEQUIN, 0);
        harness.runStateBasedActions();
        bears.setCounterCount(CounterType.MANNEQUIN, 1);
        Permanent pyro = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyro),
                null, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void losingAbilitiesSuppressesMannequinTrigger() {
        Permanent bears = reanimate(new GrizzlyBears());
        Permanent lignify = harness.addToBattlefieldAndReturn(player1, new Lignify());
        lignify.setAttachedTo(bears.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }
}
