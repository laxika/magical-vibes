package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BasrisAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each of up to two other creatures you control")
    void putsCountersOnTwoOtherControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        UUID bearsId = battlefield.get(0).getId();
        UUID elvesId = battlefield.get(1).getId();
        harness.castCreature(player1, 0, List.of(bearsId, elvesId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Llanowar Elves")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can choose only one other creature")
    void canChooseOneOtherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castCreature(player1, 0, List.of(bearsId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast without targets")
    void canBeCastWithoutTargets() {
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Basri's Acolyte");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an opposing creature")
    void cannotTargetOpposingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID opposingCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opposingCreatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }
}
