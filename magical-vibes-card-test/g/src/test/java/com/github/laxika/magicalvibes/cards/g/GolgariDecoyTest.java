package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IzzetKeyrune;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GolgariDecoy.class, DrudgeBeetle.class, IzzetKeyrune.class})
class GolgariDecoyTest extends BaseCardTest {

    private void readyScavenge() {
        harness.setGraveyard(player1, List.of(new GolgariDecoy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("All able creatures must block Golgari Decoy")
    void allAbleCreaturesMustBlock() {
        Permanent decoy = addCreatureReady(player1, new GolgariDecoy());
        decoy.setAttacking(true);

        addCreatureReady(player2, new DrudgeBeetle());
        addCreatureReady(player2, new DrudgeBeetle());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Scavenge puts +1/+1 counters equal to Golgari Decoy's power (2) on target creature")
    void scavengePutsCountersEqualToPower() {
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        harness.assertNotInGraveyard(player1, "Golgari Decoy");
    }

    @Test
    @DisplayName("Scavenge requires a creature target")
    void scavengeRequiresCreatureTarget() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new IzzetKeyrune());
        readyScavenge();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scavenge can only be activated as a sorcery")
    void scavengeIsSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        harness.setGraveyard(player1, List.of(new GolgariDecoy()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedCreaturesAreNotRequiredToBlock() {
        Permanent decoy = addCreatureReady(player1, new GolgariDecoy());
        decoy.setAttacking(true);
        Permanent tapped = addCreatureReady(player2, new DrudgeBeetle());
        tapped.tap();
        Permanent blocker = addCreatureReady(player2, new DrudgeBeetle());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(tapped.isBlocking()).isFalse();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void blockerCanChooseBetweenTwoAttackingDecoys() {
        addCreatureReady(player1, new GolgariDecoy()).setAttacking(true);
        addCreatureReady(player1, new GolgariDecoy()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DrudgeBeetle());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void scavengeCanTargetOpponentsCreatureAndExilesSourceAsCost() {
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());
        readyScavenge();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        harness.assertNotInGraveyard(player1, "Golgari Decoy");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getName()).isEqualTo("Golgari Decoy");
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void scavengeCannotBeActivatedDuringCombat() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        readyScavenge();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Golgari Decoy");
    }

    @Test
    void scavengeRequiresTwoGreenMana() {
        Permanent target = addCreatureReady(player1, new DrudgeBeetle());
        harness.setGraveyard(player1, List.of(new GolgariDecoy()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Golgari Decoy");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
