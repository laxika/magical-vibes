package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameJavelin.class, SafeholdSentry.class, ChandraHopesBeacon.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class})
class FlameJavelinTest extends BaseCardTest {

    @Test
    @DisplayName("Flame Javelin deals 4 damage to target player")
    void deals4DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Flame Javelin deals 4 damage to target creature, destroying a 2/2")
    void deals4DamageToCreatureDestroysIt() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, sentry.getId());

        harness.assertNotOnBattlefield(player2, "Safehold Sentry");
        harness.assertInGraveyard(player2, "Safehold Sentry");
    }

    @Test
    @CardUsed({FlameJavelin.class, ChandraHopesBeacon.class})
    @DisplayName("Flame Javelin deals 4 damage to target planeswalker")
    void deals4DamageToPlaneswalker() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, chandra.getId());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @CardUsed({FlameJavelin.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
    @DisplayName("Flame Javelin deals 4 damage to target battle")
    void deals4DamageToBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, battle.getId());

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Invasion of Zendikar");
    }

    @Test
    @DisplayName("Cannot cast Flame Javelin without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new FlameJavelin()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flame Javelin goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Flame Javelin");
    }

    @Test
    @DisplayName("Flame Javelin can be cast with six generic mana")
    void canBeCastWithSixGenericMana() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Flame Javelin can be cast with two red and two blue mana")
    void canBeCastWithTwoRedAndTwoBlueMana() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Flame Javelin");
    }

    @Test
    @DisplayName("Flame Javelin can be cast with one red and four colorless mana")
    void canBeCastWithOneRedAndFourColorlessMana() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Flame Javelin");
    }

    @Test
    @DisplayName("Five nonred mana cannot pay for Flame Javelin")
    void cannotBeCastWithFiveNonredMana() {
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.assertInHand(player1, "Flame Javelin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flame Javelin can target its controller")
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
        harness.assertInGraveyard(player1, "Flame Javelin");
    }
}
