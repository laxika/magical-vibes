package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathbringerThoctar.class, CruelEdict.class, GrizzlyBears.class})
class DeathbringerThoctarTest extends BaseCardTest {

    private Permanent thoctar(Player player) {
        return findPermanent(player, "Deathbringer Thoctar");
    }

    private Permanent addReadyThoctar(Player player, int counters) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DeathbringerThoctar());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return perm;
    }

    @Test
    @DisplayName("When another creature dies, may put a +1/+1 counter on Deathbringer Thoctar (accept)")
    void anotherCreatureDiesAccept() {
        harness.addToBattlefield(player1, new DeathbringerThoctar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve trigger → MayEffect prompts

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(thoctar(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When another creature dies, may decline the counter")
    void anotherCreatureDiesDecline() {
        harness.addToBattlefield(player1, new DeathbringerThoctar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(thoctar(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Deathbringer Thoctar does not trigger on its own death (only 'another creature')")
    void doesNotTriggerWhenSelfDies() {
        harness.addToBattlefield(player1, new DeathbringerThoctar());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Ability deals 1 damage to a creature and removes a +1/+1 counter as cost")
    void abilityDealsDamageToCreatureAndRemovesCounter() {
        Permanent thoctar = addReadyThoctar(player1, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(thoctar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability deals 1 damage to a player")
    void abilityDealsDamageToPlayer() {
        addReadyThoctar(player1, 1);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot activate the ability with no +1/+1 counters to remove")
    void cannotActivateWithoutCounters() {
        addReadyThoctar(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Thoctar can activate and pays its counter before resolution")
    void tappedSummoningSickThoctarCanActivate() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DeathbringerThoctar());
        source.setSummoningSick(true);
        source.tap();
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Killing another creature with the ability can replenish the spent counter")
    void killingCreatureReplenishesCounter() {
        Permanent source = addReadyThoctar(player1, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another creature controlled by Thoctar's controller dying also triggers")
    void ownCreatureDiesTriggers() {
        Permanent source = addReadyThoctar(player1, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
