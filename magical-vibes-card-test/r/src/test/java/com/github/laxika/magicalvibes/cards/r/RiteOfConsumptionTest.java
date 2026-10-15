package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
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

@CardUsed({RiteOfConsumption.class, BriarberryCohort.class, GarrukWildspeaker.class,
        SafeholdSentry.class})
class RiteOfConsumptionTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Deals damage equal to sacrificed creature's power and controller gains that much life")
    void dealsDamageAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry()); // 2/2

        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18); // 2 damage
        harness.assertLife(player1, 22); // gained 2 life
        harness.assertInGraveyard(player1, "Safehold Sentry");
    }

    @Test
    @DisplayName("Damage and life gain scale with the sacrificed creature's power including counters")
    void scalesWithCounters() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry()); // 2/2
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // becomes 5/5

        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15); // 5 damage
        harness.assertLife(player1, 25); // gained 5 life
    }

    @Test
    @DisplayName("Can target yourself (target player)")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort()); // 1/1

        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();

        // 1 damage to self, then gain 1 life: net back to 20
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals damage to a target planeswalker and gains that much life")
    void dealsDamageToPlaneswalkerAndGainsLife() {
        harness.setLife(player1, 20);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry()); // 2/2

        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, planeswalker.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains life equal to damage actually dealt when damage is prevented")
    void gainsLifeEqualToDamageActuallyDealt() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry()); // 2/2
        gd.playerDamagePreventionShields.put(player2.getId(), 2);

        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Partial prevention reduces the life gained to the damage actually dealt")
    void partialPreventionReducesLifeGain() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry());
        gd.playerDamagePreventionShields.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Uses the sacrificed creature's power on the battlefield including continuous bonuses")
    void usesPowerBeforeSacrifice() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());
        harness.addToBattlefield(player1, new BriarberryCohort());
        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.assertInGraveyard(player1, "Briarberry Cohort");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature to pay the additional cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, player2.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Safehold Sentry");
    }

    @Test
    @DisplayName("Life gain completes before checking whether self-inflicted damage is lethal")
    void survivesTemporarilyLethalSelfDamage() {
        harness.setLife(player1, 1);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry());
        harness.setHand(player1, List.of(new RiteOfConsumption()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        assertThat(gd.gameResult).isNull();
    }
}
