package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Conviction;
import com.github.laxika.magicalvibes.cards.h.HornetCannon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeFeeder.class, HornetCannon.class, Conviction.class})
class SpikeFeederTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two +1/+1 counters")
    void entersWithTwoPlusOneCounters() {
        harness.setHand(player1, List.of(new SpikeFeeder()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent feeder = findPermanent(player1, "Spike Feeder");
        assertThat(feeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes a +1/+1 counter to put one on target creature")
    void removesCounterAndPutsCounterOnTargetCreature() {
        Permanent feeder = addReadyFeeder(player1);
        Permanent target = harness.enterBattlefieldAndReturn(player2, new SpikeFeeder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(feeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Its counter ability resolves after removing its last counter")
    void counterAbilityResolvesAfterRemovingLastCounter() {
        Permanent feeder = addReadyFeeder(player1);
        feeder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.enterBattlefieldAndReturn(player2, new SpikeFeeder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(feeder);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes a +1/+1 counter to gain 2 life")
    void removesCounterAndGainsLife() {
        Permanent feeder = addReadyFeeder(player1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(feeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Can activate the life ability while summoning sick")
    void canActivateLifeAbilityWhileSummoningSick() {
        Permanent feeder = harness.enterBattlefieldAndReturn(player1, new SpikeFeeder());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(feeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyFeeder(player1);
        Permanent cannon = harness.addToBattlefieldAndReturn(player2, new HornetCannon());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cannon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate either ability without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent feeder = addReadyFeeder(player1);

        harness.setHand(player1, List.of(new Conviction()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0, feeder.getId());
        harness.passBothPriorities();

        feeder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Can target itself, paying the counter before resolution")
    void canTargetItself() {
        Permanent feeder = harness.enterBattlefieldAndReturn(player1, new SpikeFeeder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, feeder.getId());

        assertThat(feeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(feeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Targeting itself with its last counter cannot save it")
    void targetingItselfWithLastCounterDoesNotSaveIt() {
        Permanent feeder = harness.enterBattlefieldAndReturn(player1, new SpikeFeeder());
        feeder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, feeder.getId());

        harness.assertNotOnBattlefield(player1, "Spike Feeder");
        harness.assertInGraveyard(player1, "Spike Feeder");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Spike Feeder");
    }

    @Test
    @DisplayName("Can spend both counters before either life ability resolves")
    void bothLifeAbilitiesResolveAfterSourceDies() {
        Permanent feeder = harness.enterBattlefieldAndReturn(player1, new SpikeFeeder());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(feeder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Spike Feeder");
        harness.assertInGraveyard(player1, "Spike Feeder");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyFeeder(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = addCreatureReady(player, new SpikeFeeder());
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        return perm;
    }
}
