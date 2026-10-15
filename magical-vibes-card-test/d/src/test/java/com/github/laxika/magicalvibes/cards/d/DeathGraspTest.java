package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathGrasp.class, AngelfireCrusader.class, GarrukWildspeaker.class})
class DeathGraspTest extends BaseCardTest {
    @Test
    @DisplayName("X zero deals no damage and gains no life")
    void zeroXDoesNotChangeLife() {
        harness.setHand(player1, List.of(new DeathGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Death Grasp");
    }

    @Test
    @DisplayName("Targeting yourself gains life before checking for a loss at zero life")
    void canTargetSelfAndSurviveDamageExceedingLifeTotal() {
        harness.setHand(player1, List.of(new DeathGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 2);

        harness.castAndResolveSorcery(player1, 0, 3, player1.getId());

        harness.assertLife(player1, 2);
        assertThat(gd.gameResult).isNull();
        harness.assertInGraveyard(player1, "Death Grasp");
    }

    @Test
    @DisplayName("Life gained is not capped by the target creature's toughness")
    void gainsFullXWhenDamageExceedsCreatureToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AngelfireCrusader());
        harness.setHand(player1, List.of(new DeathGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setLife(player1, 10);

        harness.castAndResolveSorcery(player1, 0, 5, creature.getId());

        harness.assertInGraveyard(player2, "Angelfire Crusader");
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Deals X damage to target player and controller gains X life")
    void dealsDamageToPlayerAndGainsLife() {
        harness.setHand(player1, List.of(new DeathGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 4, player2.getId());

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Deals X damage to target creature and controller gains X life")
    void dealsDamageToCreatureAndGainsLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AngelfireCrusader());
        harness.setHand(player1, List.of(new DeathGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 10);

        harness.castAndResolveSorcery(player1, 0, 3, creature.getId());

        harness.assertNotOnBattlefield(player2, "Angelfire Crusader");
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Gains no life when its target becomes illegal before resolution")
    void gainsNoLifeWhenTargetBecomesIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AngelfireCrusader());
        harness.setHand(player1, List.of(new DeathGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player1, 10);

        harness.castSorcery(player1, 0, 2, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Deals X damage to target planeswalker and controller gains X life")
    void dealsDamageToPlaneswalkerAndGainsLife() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new DeathGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player1, 10);

        harness.castAndResolveSorcery(player1, 0, 2, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Controller gains X life even when the damage is prevented")
    void gainsXLifeEvenWhenDamageIsPrevented() {
        harness.setHand(player1, List.of(new DeathGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        gd.playerDamagePreventionShields.put(player2.getId(), 3);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 13);
    }
}
