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

@CardUsed({DregMangler.class, DrudgeBeetle.class, Mountain.class})
class DregManglerTest extends BaseCardTest {

    private void readyScavenge() {
        harness.setGraveyard(player1, List.of(new DregMangler()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Scavenge puts +1/+1 counters equal to Dreg Mangler's power (3) on target creature")
    void scavengePutsCountersEqualToPower() {
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, beetle.getId());
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(5);
    }

    @Test
    @DisplayName("Scavenge exiles Dreg Mangler as a cost, so it leaves the graveyard")
    void scavengeExilesTheCard() {
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, beetle.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Dreg Mangler");
    }

    @Test
    @DisplayName("Scavenge can target an opponent's creature")
    void scavengeCanTargetOpponentCreature() {
        Permanent beetle = addCreatureReady(player2, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, beetle.getId());
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
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
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, beetle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dreg Mangler can attack on the turn it enters")
    void hasteAllowsImmediateAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DregMangler(), "{1}{B}{G}");
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(findPermanent(player1, "Dreg Mangler").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Scavenge pays exile before resolution and cannot reuse the exiled card")
    void exileIsPaidImmediately() {
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, beetle.getId());

        harness.assertNotInGraveyard(player1, "Dreg Mangler");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isInstanceOf(DregMangler.class);
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, beetle.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Scavenge cannot be activated during its controller's upkeep")
    void scavengeRequiresMainPhase() {
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, beetle.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Dreg Mangler");
        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Scavenge requires an empty stack even during its controller's main phase")
    void scavengeRequiresEmptyStack() {
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        harness.castFromHand(player1, new DrudgeBeetle(), "{1}{G}");

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, beetle.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Dreg Mangler");
        harness.passBothPriorities();
        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Scavenge cannot be paid with only the creature's casting cost")
    void scavengeRequiresFullActivationCost() {
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, beetle.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Dreg Mangler");
        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
