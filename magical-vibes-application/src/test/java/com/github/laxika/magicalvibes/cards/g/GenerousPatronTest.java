package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BondBeetle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenerousPatron.class, BondBeetle.class, Forest.class, GrizzlyBears.class, Plains.class})
class GenerousPatronTest extends BaseCardTest {

    @Test
    void supportsTwoOtherCreaturesAndDrawsForOpponentCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GenerousPatron()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addPatronMana();

        harness.castCreature(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
    }

    @Test
    void doesNotDrawWhenPuttingCountersOnControlledCreature() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new GenerousPatron(), "{2}{G}");
        resolveAllTriggers();

        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, ownCreature.getId());
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotSupportNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new GenerousPatron()));
        addPatronMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsOnceForEachOfTwoOpposingCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GenerousPatron());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GenerousPatron());
        harness.setHand(player1, List.of(new GenerousPatron()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addPatronMana();

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void canDeclineSupportEvenWithAnOpposingCreature() {
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GenerousPatron());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new GenerousPatron(), "{2}{G}");
        resolveAllTriggers();

        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Generous Patron");
    }

    @Test
    void eachControlledPatronDrawsForTheSameCounterPlacement() {
        harness.addToBattlefield(player1, new GenerousPatron());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GenerousPatron());
        harness.setHand(player1, List.of(new GenerousPatron()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addPatronMana();

        harness.castCreature(player1, 0, opposing.getId());
        resolveAllTriggers();

        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotDrawForCountersPlacedByOpponent() {
        Permanent patron = harness.addToBattlefieldAndReturn(player1, new GenerousPatron());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GenerousPatron()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0, patron.getId());
        resolveAllTriggers();

        assertThat(patron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void canSupportAnotherPatronWithoutSupportingItself() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GenerousPatron());
        GenerousPatron entering = new GenerousPatron();
        harness.setHand(player1, List.of(entering));
        harness.setLibrary(player1, List.of(new Forest()));
        addPatronMana();

        harness.castCreature(player1, 0, other.getId());
        resolveAllTriggers();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(entering.getId()))
                .singleElement().satisfies(permanent ->
                        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addPatronMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
