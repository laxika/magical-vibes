package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Slitherhead.class, DrudgeBeetle.class, Mountain.class})
class SlitherheadTest extends BaseCardTest {

    private void readyScavenge() {
        harness.setGraveyard(player1, List.of(new Slitherhead()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Scavenge puts +1/+1 counters equal to Slitherhead's power (1) on target creature")
    void scavengePutsCountersEqualToPower() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Scavenge exiles Slitherhead as a cost, so it leaves the graveyard")
    void scavengeExilesTheCard() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Slitherhead");
    }

    @Test
    @DisplayName("Scavenge can target an opponent's creature")
    void scavengeCanTargetOpponentCreature() {
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        harness.setGraveyard(player1, List.of(new Slitherhead()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void scavengeExilesSourceBeforeResolutionWithoutMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        readyScavenge();
        var source = gd.playerGraveyards.get(player1.getId()).getFirst();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        harness.assertNotInGraveyard(player1, "Slitherhead");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void scavengeCannotActivateDuringCombat() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        readyScavenge();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Slitherhead");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scavengeCannotActivateWithAnAbilityOnTheStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        readyScavenge();
        harness.setGraveyard(player1, List.of(new Slitherhead(), new Slitherhead()));
        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void scavengeCanActivateInPostcombatMainPhase() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        readyScavenge();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void scavengeDoesNotRefundExileWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        readyScavenge();
        var source = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.activateGraveyardAbility(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        harness.assertNotInGraveyard(player1, "Slitherhead");
        assertThat(gd.stack).isEmpty();
    }
}
