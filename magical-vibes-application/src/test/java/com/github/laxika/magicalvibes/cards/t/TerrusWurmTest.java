package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.a.AnnihilatingFire;
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

@CardUsed({TerrusWurm.class, DrudgeBeetle.class, Mountain.class, AnnihilatingFire.class})
class TerrusWurmTest extends BaseCardTest {

    private void readyScavenge() {
        harness.setGraveyard(player1, List.of(new TerrusWurm()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    @Test
    @DisplayName("Scavenge puts +1/+1 counters equal to Terrus Wurm's power (5) on target creature")
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
    @DisplayName("Scavenge exiles Terrus Wurm as a cost, so it leaves the graveyard")
    void scavengeExilesTheCard() {
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Terrus Wurm");
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
        harness.setGraveyard(player1, List.of(new TerrusWurm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void scavengePaysCostsBeforeResolving() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        var wurmId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        harness.assertNotInGraveyard(player1, "Terrus Wurm");
        assertThat(gd.findExiledCard(wurmId)).isNotNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void scavengeRequiresSevenManaIncludingBlack() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        harness.setGraveyard(player1, List.of(new TerrusWurm()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Terrus Wurm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scavengeCannotBeActivatedDuringUpkeep() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Terrus Wurm");
    }

    @Test
    void scavengeCannotBeActivatedWithNonemptyStack() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Terrus Wurm");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void removedTargetDoesNotRefundScavengeCost() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        var wurmId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.setHand(player2, List.of(new AnnihilatingFire()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertNotOnBattlefield(player1, "Drudge Beetle");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.findExiledCard(wurmId)).isNotNull();
        harness.assertNotInGraveyard(player1, "Terrus Wurm");
    }
}
