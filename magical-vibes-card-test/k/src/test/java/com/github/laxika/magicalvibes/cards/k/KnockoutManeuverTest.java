package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnockoutManeuver.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class})
class KnockoutManeuverTest extends BaseCardTest {

    @Test
    @DisplayName("The counter is placed before the creature deals power damage")
    void counterIsPlacedBeforePowerDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new KnockoutManeuver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID sourceId = source.getId();
        UUID targetId = target.getId();
        harness.castSorcery(player1, 0, List.of(sourceId, targetId));
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The first target must be a creature you control")
    void cannotTargetOpponentCreatureFirst() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new KnockoutManeuver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID sourceId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(sourceId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("The second target must be a creature controlled by an opponent")
    void cannotTargetOwnCreatureSecond() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new KnockoutManeuver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(sourceId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("The counter is still placed when the opposing target leaves")
    void counterIsPlacedWhenVictimLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new KnockoutManeuver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Knockout Maneuver");
    }

    @Test
    @DisplayName("No damage is dealt when the source creature leaves")
    void noDamageWhenSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new KnockoutManeuver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Knockout Maneuver");
    }

    @Test
    @DisplayName("Damage is one-sided and can destroy the opposing creature")
    void lethalDamageDoesNotCauseReturnDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new KnockoutManeuver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, List.of(source.getId(), targetId));
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
