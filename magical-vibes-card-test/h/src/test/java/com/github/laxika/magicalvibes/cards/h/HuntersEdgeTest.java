package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed({HuntersEdge.class, GrizzlyBears.class, AirElemental.class, LlanowarElves.class})
class HuntersEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on the creature, then deals its increased power as damage")
    void counterIncreasesDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new HuntersEdge()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, elementalId));

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getMarkedDamage()).isZero();

        Permanent elemental = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The counter is applied before the damage, allowing the spell to kill a 3/3")
    void counterAppliesBeforeDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntersEdge()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameData().playerBattlefields.get(player2.getId()).getFirst()
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(sourceId, targetId));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Targets must be a creature you control and a creature you don't control")
    void enforcesTargetRestrictions() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntersEdge()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(firstId, secondId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }

    @Test
    @DisplayName("The counter still resolves when the opposing target leaves")
    void counterResolvesWhenSecondTargetLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntersEdge()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(sourceId, targetId));
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No damage is dealt when the creature you control leaves before resolution")
    void noDamageWhenFirstTargetLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntersEdge()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castSorcery(player1, 0, List.of(sourceId, targetId));
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        Permanent target = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Hunter's Edge");
    }

    @Test
    @DisplayName("The first target must be a creature you control")
    void rejectsOpposingCreatureAsFirstTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntersEdge()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID sourceId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(sourceId, targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opposing target that comes under your control takes no damage")
    void counterResolvesWhenSecondTargetChangesController() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntersEdge()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        Permanent source = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        Permanent target = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        harness.castSorcery(player1, 0, List.of(sourceId, targetId));
        harness.getGameData().playerBattlefields.get(player2.getId()).remove(target);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }
}
