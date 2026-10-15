package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.a.AutarchMammoth;
import com.github.laxika.magicalvibes.cards.b.BeastriderVanguard;
import com.github.laxika.magicalvibes.cards.c.CryptcallerChariot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JibbirikOmnivore;
import com.github.laxika.magicalvibes.cards.v.ValorsFlagship;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderousVelocipede.class, AirResponseUnit.class, AutarchMammoth.class,
        BeastriderVanguard.class, CryptcallerChariot.class, JibbirikOmnivore.class,
        ValorsFlagship.class, Forest.class})
class ThunderousVelocipedeTest extends BaseCardTest {

    @Test
    void givesOneCounterToCreaturesAndVehiclesWithManaValueAtMostFour() {
        harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());

        Permanent creature = cast(player1, new BeastriderVanguard(), "{1}{G}");
        Permanent vehicle = cast(player1, new AirResponseUnit(), "{2}{W}");

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void givesThreeCountersToCreaturesAndVehiclesWithManaValueAboveFour() {
        harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());

        Permanent creature = cast(player1, new AutarchMammoth(), "{4}{G}{G}");
        resolveAllTriggers();
        Permanent vehicle = cast(player1, new ValorsFlagship(), "{4}{W}{W}{W}");

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotAffectOtherPlayersOrNonCreatureNonVehicles() {
        harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());

        harness.forceActivePlayer(player2);
        Permanent opponentCreature = cast(player2, new BeastriderVanguard(), "{1}{G}");
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Forest");

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void manaValueFourVehicleEntersWithOneCounterBeforeAnyTriggerResolves() {
        harness.addToBattlefield(player1, new ThunderousVelocipede());

        Permanent vehicle = cast(player1, new CryptcallerChariot(), "{3}{B}");

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    void doesNotGiveItselfCountersButAnotherCopyDoes() {
        Permanent first = cast(player1, new ThunderousVelocipede(), "{1}{G}{G}");
        Permanent second = harness.enterBattlefieldAndReturn(player1, new ThunderousVelocipede());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void multipleCopiesAddTheirCountersAndTokensReceiveCounters() {
        harness.addToBattlefield(player1, new ThunderousVelocipede());
        harness.addToBattlefield(player1, new ThunderousVelocipede());

        Permanent creature = cast(player1, new AutarchMammoth(), "{4}{G}{G}");
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(findPermanent(player1, "Elephant").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void summoningSickCreatureCanCrewAndAnimationEndsAtEndOfTurn() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new JibbirikOmnivore());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    void crewedVehicleTramplesOverBlocker() {
        addCreatureReady(player1, new ThunderousVelocipede());
        harness.addToBattlefield(player1, new JibbirikOmnivore());
        Permanent blocker = addCreatureReady(player2, new BeastriderVanguard());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertOnBattlefield(player1, "Thunderous Velocipede");
    }

    @Test
    void cannotCrewWithOnlyTwoPower() {
        harness.addToBattlefield(player1, new ThunderousVelocipede());
        harness.addToBattlefield(player1, new BeastriderVanguard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent cast(Player player, Card card, String manaCost) {
        harness.castFromHand(player, card, manaCost);
        harness.passBothPriorities();
        return findPermanent(player, card.getName());
    }
}
