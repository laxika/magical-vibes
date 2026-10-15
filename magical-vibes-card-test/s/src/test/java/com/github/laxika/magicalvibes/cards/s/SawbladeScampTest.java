package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawbladeScamp.class, LightningBolt.class, GrizzlyBears.class})
class SawbladeScampTest extends BaseCardTest {

    @Test
    void castingANoncreatureSpellPutsAnOilCounterOnSawbladeScamp() {
        Permanent scamp = addReadyScamp();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(scamp.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void castingACreatureSpellDoesNotPutAnOilCounterOnSawbladeScamp() {
        Permanent scamp = addReadyScamp();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(scamp.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    void removingAnOilCounterDealsDamageToEachOpponent() {
        Permanent scamp = addReadyScamp();
        scamp.setCounterCount(CounterType.OIL, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scamp.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void cannotActivateWithoutAnOilCounter() {
        addReadyScamp();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotAddOil() {
        Permanent scamp = addReadyScamp();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(scamp.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    void oilTriggerResolvesBeforeTheSpell() {
        Permanent scamp = addReadyScamp();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(scamp.getCounterCount(CounterType.OIL)).isZero();
        harness.passBothPriorities();

        assertThat(scamp.getCounterCount(CounterType.OIL)).isEqualTo(1);
        harness.assertLife(player2, 20);
        resolveAllTriggers();
        harness.assertLife(player2, 17);
    }

    @Test
    void activationPaysCostsImmediatelyAndCannotBeRepeatedWhileTapped() {
        Permanent scamp = addReadyScamp();
        scamp.setCounterCount(CounterType.OIL, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(scamp.isTapped()).isTrue();
        assertThat(scamp.getCounterCount(CounterType.OIL)).isEqualTo(1);
        harness.assertLife(player2, 20);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        resolveAllTriggers();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void activatedAbilityStillDealsDamageAfterScampDies() {
        Permanent scamp = addReadyScamp();
        scamp.setCounterCount(CounterType.OIL, 1);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, scamp.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sawblade Scamp");
        harness.assertLife(player2, 20);
        resolveAllTriggers();
        harness.assertLife(player2, 19);
    }

    @Test
    void hasteAllowsActivationOnTheTurnScampEnters() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SawbladeScamp(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
    }

    private Permanent addReadyScamp() {
        Permanent scamp = addCreatureReady(player1, new SawbladeScamp());
        scamp.setCounterCount(CounterType.OIL, 0);
        return scamp;
    }
}
