package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.v.VivienArkbowRanger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GruesomeScourger.class, BaronyVampire.class, VivienArkbowRanger.class, Murder.class})
class GruesomeScourgerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage equal to the number of creatures controlled")
    void dealsDamageEqualToCreaturesControlled() {
        harness.addToBattlefield(player1, new BaronyVampire());
        harness.addToBattlefield(player1, new BaronyVampire());
        harness.addToBattlefield(player2, new BaronyVampire());
        harness.setHand(player1, List.of(new GruesomeScourger()));
        addManaToCast();

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("ETB can deal damage to an opponent's planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new VivienArkbowRanger());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.addToBattlefield(player1, new BaronyVampire());
        harness.setHand(player1, List.of(new GruesomeScourger()));
        addManaToCast();

        harness.castCreature(player1, 0, 0, planeswalker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB counts only creatures controlled by its controller")
    void countsOnlyControllerCreatures() {
        harness.addToBattlefield(player1, new BaronyVampire());
        harness.addToBattlefield(player2, new BaronyVampire());
        harness.addToBattlefield(player2, new BaronyVampire());
        harness.setHand(player1, List.of(new GruesomeScourger()));
        addManaToCast();

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new GruesomeScourger()));
        addManaToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB counts Gruesome Scourger itself")
    void countsItself() {
        harness.setHand(player1, List.of(new GruesomeScourger()));
        addManaToCast();

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("ETB can target a planeswalker controlled by its controller")
    void canTargetOwnPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new VivienArkbowRanger());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new GruesomeScourger()));
        addManaToCast();

        harness.castCreature(player1, 0, 0, planeswalker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BaronyVampire());
        harness.setHand(player1, List.of(new GruesomeScourger()));
        addManaToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB counts creatures at resolution after another creature dies")
    void countsCreaturesAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BaronyVampire());
        harness.setHand(player1, List.of(new GruesomeScourger()));
        harness.setHand(player2, List.of(new Murder()));
        addManaToCast();

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Barony Vampire");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("ETB still resolves after Gruesome Scourger dies and does not count it")
    void resolvesAfterSourceDies() {
        harness.addToBattlefield(player1, new BaronyVampire());
        harness.setHand(player1, List.of(new GruesomeScourger()));
        harness.setHand(player2, List.of(new Murder()));
        addManaToCast();

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        Permanent scourger = findPermanent(player1, "Gruesome Scourger");
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, scourger.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gruesome Scourger");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("ETB deals zero damage if no creatures remain at resolution")
    void dealsZeroWithNoCreaturesRemaining() {
        harness.setHand(player1, List.of(new GruesomeScourger()));
        harness.setHand(player2, List.of(new Murder()));
        addManaToCast();

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        Permanent scourger = findPermanent(player1, "Gruesome Scourger");
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, scourger.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gruesome Scourger");
        harness.assertLife(player2, 20);
    }

    private void addManaToCast() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
