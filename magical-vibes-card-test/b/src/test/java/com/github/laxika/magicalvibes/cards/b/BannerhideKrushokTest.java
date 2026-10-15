package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BannerhideKrushok.class, Forest.class, GrizzlyBears.class})
class BannerhideKrushokTest extends BaseCardTest {

    @Test
    void reinforceCanTargetOpponentsCreatureDuringTheirTurn() {
        harness.setHand(player1, List.of(new BannerhideKrushok()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, bears.getId());
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void reinforceRejectsLandWithoutDiscardingSource() {
        harness.setHand(player1, List.of(new BannerhideKrushok()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Bannerhide Krushok");
        harness.assertNotInGraveyard(player1, "Bannerhide Krushok");
    }

    @Test
    void scavengeExilesSourceBeforeResolvingAndCanTargetOpponentsCreature() {
        BannerhideKrushok krushok = new BannerhideKrushok();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(krushok));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Bannerhide Krushok");
        assertThat(gd.exiledCards).anySatisfy(entry ->
                assertThat(entry.card().getId()).isEqualTo(krushok.getId()));
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void scavengeRejectsActivationDuringUpkeep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new BannerhideKrushok()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Bannerhide Krushok");
    }

    @Test
    void scavengeRejectsActivationWithAnAbilityOnTheStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BannerhideKrushok()));
        harness.setGraveyard(player1, List.of(new BannerhideKrushok()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateHandAbility(player1, 0, bears.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Bannerhide Krushok");
    }

    @Test
    @DisplayName("Reinforce puts two +1/+1 counters on target creature")
    void reinforceBoostsTargetCreature() {
        harness.setHand(player1, List.of(new BannerhideKrushok()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Reinforce discards Bannerhide Krushok to the graveyard as a cost")
    void reinforceDiscardsSourceCard() {
        harness.setHand(player1, List.of(new BannerhideKrushok()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, bears.getId());

        harness.assertNotInHand(player1, "Bannerhide Krushok");
        harness.assertInGraveyard(player1, "Bannerhide Krushok");
    }

    @Test
    @DisplayName("Scavenge puts four +1/+1 counters equal to Bannerhide Krushok's power on target creature")
    void scavengePutsCountersEqualToPower() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new BannerhideKrushok()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bears.getEffectivePower()).isEqualTo(6);
        assertThat(bears.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Scavenge requires a creature target")
    void scavengeRejectsNonCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setGraveyard(player1, List.of(new BannerhideKrushok()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scavenge can only be activated as a sorcery")
    void scavengeIsSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new BannerhideKrushok()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
