package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({DeadbridgeGoliath.class, DrudgeBeetle.class, Mountain.class})
class DeadbridgeGoliathTest extends BaseCardTest {

    private void readyScavenge() {
        harness.setGraveyard(player1, List.of(new DeadbridgeGoliath()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Scavenge puts +1/+1 counters equal to Deadbridge Goliath's power (5) on target creature")
    void scavengePutsCountersEqualToPower() {
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
    }

    @Test
    @DisplayName("Scavenge exiles Deadbridge Goliath as a cost, so it leaves the graveyard")
    void scavengeExilesTheCard() {
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Deadbridge Goliath");
    }

    @Test
    @DisplayName("Scavenge can target an opponent's creature")
    void scavengeCanTargetOpponentCreature() {
        Permanent bears = addCreatureReady(player2, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Scavenge requires a creature target")
    void scavengeRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyScavenge();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scavenge can only be activated as a sorcery")
    void scavengeIsSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        harness.setGraveyard(player1, List.of(new DeadbridgeGoliath()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void scavengePaysExileCostBeforeResolving() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        var source = gd.playerGraveyards.get(player1.getId()).getFirst();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        harness.assertNotInGraveyard(player1, "Deadbridge Goliath");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
    }

    @Test
    void scavengeRequiresTwoGreenMana() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        harness.setGraveyard(player1, List.of(new DeadbridgeGoliath()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Deadbridge Goliath");
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void scavengeRequiresSixManaTotal() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        harness.setGraveyard(player1, List.of(new DeadbridgeGoliath()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Deadbridge Goliath");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scavengeCannotActivateDuringCombat() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Deadbridge Goliath");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scavengeCannotActivateWithAnAbilityOnTheStack() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        harness.setGraveyard(player1, List.of(new DeadbridgeGoliath(), new DeadbridgeGoliath()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void scavengeCanActivateInPostcombatMainPhase() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }
}
