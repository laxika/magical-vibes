package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonclawStrike.class, GrizzlyBears.class, HillGiant.class, Unsummon.class})
class DragonclawStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles a target creature's power and toughness without a fight target")
    void doublesTargetWithoutFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonclawStrike()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId));

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Doubles the creature before it fights")
    void doublesBeforeFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DragonclawStrike()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, giantId));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The doubling wears off at end of turn")
    void doublingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonclawStrike()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId));
        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects an opponent's creature as the first target")
    void firstTargetMustBeControlled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonclawStrike()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID opponentId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Rejects a creature you control as the optional fight target")
    void fightTargetMustBeControlledByOpponent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new DragonclawStrike()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(battlefield.get(0).getId(), battlefield.get(1).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Still doubles the controlled creature when the fight target leaves")
    void doublesWhenFightTargetLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DragonclawStrike()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player2, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(bearId, giantId));
        harness.castAndResolveInstant(player2, 0, giantId);
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Dragonclaw Strike");
    }

    @Test
    @DisplayName("Does not double or damage the opponent's creature when the first target leaves")
    void noFightWhenControlledTargetLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DragonclawStrike()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player2, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(bearId, giantId));
        harness.castAndResolveInstant(player2, 0, bearId);
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dragonclaw Strike");
    }

    @Test
    @DisplayName("Doubles current power and toughness including counters, without doubling the counters")
    void doublesCurrentStatsWithCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new DragonclawStrike()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId()));

        assertThat(bear.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectiveToughness()).isEqualTo(6);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        assertThat(bear.getEffectivePower()).isEqualTo(7);
        assertThat(bear.getEffectiveToughness()).isEqualTo(7);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
