package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MosquitoGuard.class, GrizzlyBears.class, Forest.class})
class MosquitoGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Reinforce requires white mana even when enough total mana is available")
    void reinforceRequiresWhiteMana() {
        harness.setHand(player1, List.of(new MosquitoGuard()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MosquitoGuard());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mosquito Guard");
        harness.assertNotInGraveyard(player1, "Mosquito Guard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce works during the opponent's upkeep and resolves through the stack")
    void reinforceWorksDuringOpponentsTurn() {
        harness.setHand(player1, List.of(new MosquitoGuard()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MosquitoGuard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Mosquito Guard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A reinforced Mosquito Guard kills a 2/2 blocker with first strike and survives")
    void reinforcedGuardDealsFirstStrikeDamage() {
        Permanent guard = addCreatureReady(player1, new MosquitoGuard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MosquitoGuard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateHandAbility(player1, 0, guard.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Mosquito Guard");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(guard.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Reinforce puts a +1/+1 counter on target creature")
    void reinforceBoostsTargetCreature() {
        harness.setHand(player1, List.of(new MosquitoGuard()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Reinforce can target an opponent's creature")
    void reinforceCanTargetOpponentsCreature() {
        harness.setHand(player1, List.of(new MosquitoGuard()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Reinforce discards the source card to the graveyard as a cost")
    void reinforceDiscardsSourceCard() {
        harness.setHand(player1, List.of(new MosquitoGuard()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, bears.getId());

        harness.assertNotInHand(player1, "Mosquito Guard");
        harness.assertInGraveyard(player1, "Mosquito Guard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Reinforce cannot be activated without enough mana; the card stays in hand")
    void reinforceRequiresMana() {
        harness.setHand(player1, List.of(new MosquitoGuard()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Mosquito Guard");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce cannot target a non-creature permanent; no cost is paid")
    void reinforceRejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new MosquitoGuard()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Mosquito Guard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
