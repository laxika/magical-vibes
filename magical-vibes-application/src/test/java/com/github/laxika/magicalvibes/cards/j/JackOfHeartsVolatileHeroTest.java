package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({JackOfHeartsVolatileHero.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class JackOfHeartsVolatileHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Power-up discards the hand, draws three cards, and puts two counters on Jack")
    void powerUpDiscardsDrawsAndAddsCounters() {
        Permanent jack = harness.enterBattlefieldAndReturn(player1, new JackOfHeartsVolatileHero());
        Card discardedOne = new Shock();
        Card discardedTwo = new Shock();
        Card drawnOne = new GrizzlyBears();
        Card drawnTwo = new GrizzlyBears();
        Card drawnThree = new GrizzlyBears();
        harness.setHand(player1, List.of(discardedOne, discardedTwo));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo, drawnThree));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(jack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo, drawnThree);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(discardedOne, discardedTwo);
    }

    @Test
    @DisplayName("Power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new JackOfHeartsVolatileHero());
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("When Jack dies, he deals damage equal to his power to each creature")
    void deathTriggerDealsDamageEqualToPowerToEachCreature() {
        Permanent jack = addCreatureReady(player1, new JackOfHeartsVolatileHero());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, jack.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jack of Hearts, Volatile Hero");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Power-up costs only three generic mana when Jack entered this turn")
    void powerUpUsesFullManaCostReductionOnEntryTurn() {
        Permanent jack = harness.enterBattlefieldAndReturn(player1, new JackOfHeartsVolatileHero());
        harness.setHand(player1, List.of());
        List<Card> drawn = List.of(new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, drawn);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(jack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power-up does not receive the entry discount for an older Jack")
    void powerUpRequiresFullCostWithoutEntryThisTurn() {
        addCreatureReady(player1, new JackOfHeartsVolatileHero());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Jack's death trigger uses his power including counters and does not damage players")
    void deathTriggerUsesLastKnownPowerWithCounters() {
        Permanent jack = addCreatureReady(player1, new JackOfHeartsVolatileHero());
        jack.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        int playerOneLife = gd.playerLifeTotals.get(player1.getId());
        int playerTwoLife = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player1, 0, jack.getId());
        harness.assertOnBattlefield(player1, "Jack of Hearts, Volatile Hero");
        harness.castAndResolveInstant(player1, 0, jack.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jack of Hearts, Volatile Hero");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertLife(player1, playerOneLife);
        harness.assertLife(player2, playerTwoLife);
    }

    @Test
    @DisplayName("Removing Jack in response to power-up still discards and draws at resolution")
    void powerUpResolvesAfterJackDies() {
        Permanent jack = harness.enterBattlefieldAndReturn(player1, new JackOfHeartsVolatileHero());
        Card discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        List<Card> drawn = List.of(new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, drawn);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discarded);
        harness.castAndResolveInstant(player2, 0, jack.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jack of Hearts, Volatile Hero");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(jack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
