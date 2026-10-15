package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchonOfTheTriumvirate.class, GrizzlyBears.class, LlanowarElves.class,
        FountainOfYouth.class, Forest.class})
class ArchonOfTheTriumvirateTest extends BaseCardTest {

    @Test
    @DisplayName("Attack detains two chosen nonland permanents")
    void attackDetainsTwoTargets() {
        Permanent archon = addReadyArchon();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        attackAndDetain(List.of(bear.getId(), elves.getId()), archon);

        assertThatThrownBy(() -> declareAttack(bear))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThatThrownBy(() -> harness.tapPermanent(player2, indexOf(player2, elves)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Can detain a noncreature nonland permanent")
    void canDetainArtifact() {
        Permanent archon = addReadyArchon();
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        attackAndDetain(List.of(fountain.getId()), archon);

        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, fountain), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Can choose zero targets (up to two)")
    void canChooseZeroTargets() {
        Permanent archon = addReadyArchon();
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(indexOf(player1, archon)));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThatCode(() -> declareAttack(bear)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Detain wears off at the Archon controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        Permanent archon = addReadyArchon();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        attackAndDetain(List.of(bear.getId()), archon);
        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(bear)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot detain a permanent you control")
    void cannotDetainOwnPermanent() {
        Permanent archon = addReadyArchon();
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(indexOf(player1, archon)));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Detained creature cannot block a ground attacker")
    void detainedCreatureCannotBlock() {
        Permanent archon = addReadyArchon();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attackAndDetain(List.of(bear.getId()), archon);

        archon.setAttacking(false);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, bear), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Detain persists through the detained permanent controller's untap")
    void detainPersistsThroughOpponentsTurn() {
        Permanent archon = addReadyArchon();
        Permanent elves = addCreatureReady(player2, new LlanowarElves());
        attackAndDetain(List.of(elves.getId()), archon);

        harness.performUntapStep(player2);

        assertThatThrownBy(() -> harness.tapPermanent(player2, indexOf(player2, elves)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Cannot choose a land as a detain target")
    void cannotDetainLand() {
        Permanent archon = addReadyArchon();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(indexOf(player1, archon)));

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose more than two detain targets")
    void cannotDetainThreeTargets() {
        Permanent archon = addReadyArchon();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(indexOf(player1, archon)));

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArchon() {
        Permanent archon = addCreatureReady(player1, new ArchonOfTheTriumvirate());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        return archon;
    }

    private void attackAndDetain(List<java.util.UUID> targetIds, Permanent archon) {
        declareAttackers(List.of(indexOf(player1, archon)));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, targetIds);
        harness.passBothPriorities();
    }

    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        declareAttackers(player2, List.of(indexOf(player2, creature)));
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
