package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraTorchOfDefiance;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandraTorchOfDefiance.class, FatefulShowdown.class, GrizzlyBears.class, Island.class, Mountain.class, WindDrake.class})
class FatefulShowdownTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the remaining hand, then discards and draws that many")
    void dealsDamageThenWheelsTheHand() {
        harness.setHand(player1, List.of(
                new FatefulShowdown(), new GrizzlyBears(), new Island(), new Mountain()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        addMana(player1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Island", "Island", "Island");
        harness.assertInGraveyard(player1, "Fateful Showdown");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Can deal the hand-size damage to a creature")
    void dealsDamageToCreatureTarget() {
        harness.addToBattlefield(player2, new WindDrake());
        var targetId = harness.getPermanentId(player2, "Wind Drake");
        harness.setHand(player1, List.of(
                new FatefulShowdown(), new GrizzlyBears(), new Island(), new Mountain()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        addMana(player1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Wind Drake");
    }

    @Test
    @DisplayName("An empty hand deals no damage and draws no cards")
    void emptyHandDoesNotDamageOrDraw() {
        harness.setHand(player1, List.of(new FatefulShowdown()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana(player1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Fateful Showdown");
    }

    @Test
    @DisplayName("Uses the hand size at resolution rather than at casting")
    void countsHandAtResolution() {
        harness.setHand(player1, List.of(new FatefulShowdown(), new Mountain()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        addMana(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player1, List.of(new Mountain(), new WindDrake()));

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Wind Drake");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An illegal sole target prevents both discarding and drawing")
    void illegalTargetDoesNotWheelHand() {
        harness.addToBattlefield(player2, new WindDrake());
        var targetId = harness.getPermanentId(player2, "Wind Drake");
        Mountain retainedCard = new Mountain();
        harness.setHand(player1, List.of(new FatefulShowdown(), retainedCard));
        harness.setLibrary(player1, List.of(new Island()));
        addMana(player1);
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Fateful Showdown");
    }

    @Test
    @DisplayName("Can target its own controller and still replace the hand")
    void canTargetController() {
        harness.setHand(player1, List.of(new FatefulShowdown(), new Mountain()));
        harness.setLibrary(player1, List.of(new Island()));
        addMana(player1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can damage a planeswalker without damaging its controller")
    void dealsDamageToPlaneswalker() {
        var chandra = harness.addToBattlefieldAndReturn(player2, new ChandraTorchOfDefiance());
        chandra.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new FatefulShowdown(), new Mountain(), new WindDrake()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        addMana(player1);

        harness.castAndResolveInstant(player1, 0, chandra.getId());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY))
                .isEqualTo(2);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Wind Drake");
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
