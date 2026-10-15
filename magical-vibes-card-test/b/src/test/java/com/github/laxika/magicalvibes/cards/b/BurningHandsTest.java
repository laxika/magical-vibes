package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.OldGnawbone;
import com.github.laxika.magicalvibes.cards.t.TrelasarraMoonDancer;
import com.github.laxika.magicalvibes.cards.z.ZarielArchdukeOfAvernus;
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

@CardUsed({BurningHands.class, GarrukWildspeaker.class, GrizzlyBears.class, HillGiant.class, OldGnawbone.class, TrelasarraMoonDancer.class, ZarielArchdukeOfAvernus.class})
class BurningHandsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a non-green creature")
    void dealsTwoDamageToNonGreenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castOn(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 6 damage to a green creature")
    void dealsSixDamageToGreenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castOn(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 6 damage to a green planeswalker")
    void dealsSixDamageToGreenPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 8);

        castOn(target);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals exactly 6 damage to a green creature that survives")
    void dealsExactlySixDamageToGreenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OldGnawbone());

        castOn(target);

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertOnBattlefield(player1, "Old Gnawbone");
    }

    @Test
    @DisplayName("Deals 6 damage to a multicolor creature that is green")
    void dealsSixDamageToMulticolorGreenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TrelasarraMoonDancer());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        castOn(target);

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertOnBattlefield(player2, "Trelasarra, Moon Dancer");
    }

    @Test
    @DisplayName("Deals 2 damage to a non-green planeswalker")
    void dealsTwoDamageToNonGreenPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZarielArchdukeOfAvernus());
        target.setCounterCount(CounterType.LOYALTY, 4);

        castOn(target);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Zariel, Archduke of Avernus");
    }

    @Test
    @DisplayName("Does not deal damage when the target has left the battlefield")
    void doesNotDamageAbsentTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OldGnawbone());
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setHand(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Old Gnawbone");
        harness.assertInGraveyard(player1, "Burning Hands");
    }

    private void castOn(Permanent target) {
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new BurningHands()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
