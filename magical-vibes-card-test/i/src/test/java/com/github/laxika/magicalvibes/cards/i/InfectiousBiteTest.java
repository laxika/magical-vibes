package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfectiousBite.class, GrizzlyBears.class, LlanowarElves.class})
class InfectiousBiteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals controlled creature's power to an opposing creature and gives each opponent a poison counter")
    void dealsPowerDamageAndPoisonsEachOpponent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new InfectiousBite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(sourceId, targetId));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage is one-sided")
    void damageIsOneSided() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new InfectiousBite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        Permanent remainingSource = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(remainingSource.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Requires a creature you control and a creature an opponent controls")
    void validatesTargetRestrictions() {
        Permanent ownSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new InfectiousBite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(ownSource.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("One target leaving prevents damage but not the poison counter")
    void oneTargetLeavingStillPoisonsOpponent(int departingTarget) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new InfectiousBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departingTarget == 0 ? source : target));
        harness.passBothPriorities();

        harness.assertOnBattlefield(departingTarget == 0 ? player2 : player1,
                departingTarget == 0 ? "Llanowar Elves" : "Grizzly Bears");
        assertThat((departingTarget == 0 ? target : source).getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Both targets leaving prevents the entire spell from resolving")
    void bothTargetsLeavingPreventsPoison() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new InfectiousBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
        });
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player1, "Infectious Bite");
    }

    @Test
    @DisplayName("Damage uses the creature's power at resolution")
    void usesPowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InfectiousBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));

        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
