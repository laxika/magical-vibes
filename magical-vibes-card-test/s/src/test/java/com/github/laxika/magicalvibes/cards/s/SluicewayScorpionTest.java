package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.ToweringIndrik;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SluicewayScorpion.class, GrizzlyBears.class, Mountain.class, ToweringIndrik.class})
class SluicewayScorpionTest extends BaseCardTest {

    private void readyScavenge() {
        harness.setGraveyard(player1, List.of(new SluicewayScorpion()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Scavenge puts +1/+1 counters equal to Sluiceway Scorpion's power (2) on target creature")
    void scavengePutsCountersEqualToPower() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Scavenge exiles Sluiceway Scorpion as a cost, so it leaves the graveyard")
    void scavengeExilesTheCard() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Sluiceway Scorpion");
    }

    @Test
    @DisplayName("Scavenge can target an opponent's creature")
    void scavengeCanTargetOpponentCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SluicewayScorpion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathtouchKillsBlockerWithMoreToughnessThanDamageDealt() {
        addCreatureReady(player1, new SluicewayScorpion());
        addCreatureReady(player2, new ToweringIndrik());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Towering Indrik");
        harness.assertInGraveyard(player1, "Sluiceway Scorpion");
        harness.assertLife(player2, 20);
    }

    @Test
    void exileCostIsPaidBeforeCountersResolve() {
        Permanent target = addCreatureReady(player1, new ToweringIndrik());
        readyScavenge();
        var sourceId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        harness.assertNotInGraveyard(player1, "Sluiceway Scorpion");
        assertThat(gd.findExiledCard(sourceId)).isNotNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void scavengeCannotBeActivatedDuringOwnUpkeep() {
        Permanent target = addCreatureReady(player1, new ToweringIndrik());
        readyScavenge();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Sluiceway Scorpion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scavengeCannotBeActivatedWhileAnotherAbilityIsOnStack() {
        Permanent target = addCreatureReady(player1, new ToweringIndrik());
        readyScavenge();
        harness.setGraveyard(player1, List.of(new SluicewayScorpion(), new SluicewayScorpion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void insufficientManaDoesNotExileScorpion() {
        Permanent target = addCreatureReady(player1, new ToweringIndrik());
        harness.setGraveyard(player1, List.of(new SluicewayScorpion()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Sluiceway Scorpion");
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
