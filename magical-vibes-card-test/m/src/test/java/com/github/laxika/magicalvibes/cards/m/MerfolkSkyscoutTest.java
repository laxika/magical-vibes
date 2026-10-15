package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EchoCirclet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkSkyscout.class, Island.class, GrizzlyBears.class, EchoCirclet.class})
class MerfolkSkyscoutTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking untaps target permanent")
    void attackingUntapsTargetPermanent() {
        addCreatureReady(player1, new MerfolkSkyscout());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        island.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, island.getId());
        harness.passBothPriorities();

        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Blocking untaps target permanent")
    void blockingUntapsTargetPermanent() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();
        addCreatureReady(player2, new MerfolkSkyscout());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.handlePermanentChosen(player2, island.getId());
        harness.passBothPriorities();

        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger can untap Skyscout itself without removing it from combat")
    void attackTriggerCanUntapItself() {
        Permanent skyscout = addCreatureReady(player1, new MerfolkSkyscout());

        declareAttackers(player1, List.of(0));
        assertThat(skyscout.isTapped()).isTrue();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handlePermanentChosen(player1, skyscout.getId());
            harness.passBothPriorities();
        });

        assertThat(skyscout.isTapped()).isFalse();
        assertThat(skyscout.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Untapped permanents are legal targets")
    void canTargetAnUntappedPermanent() {
        addCreatureReady(player1, new MerfolkSkyscout());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, island.getId());
        harness.passBothPriorities();

        assertThat(island.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attack trigger resolves even after Skyscout leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent skyscout = addCreatureReady(player1, new MerfolkSkyscout());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        island.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, island.getId());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(skyscout);
        gd.playerGraveyards.get(player1.getId()).add(skyscout.getCard());
        harness.passBothPriorities();

        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Blocking multiple attackers produces only one untap trigger")
    void blockingMultipleAttackersTriggersOnlyOnce() {
        addCreatureReady(player1, new MerfolkSkyscout()).setAttacking(true);
        addCreatureReady(player1, new MerfolkSkyscout()).setAttacking(true);
        Permanent skyscout = addCreatureReady(player2, new MerfolkSkyscout());
        Permanent circlet = harness.addToBattlefieldAndReturn(player2, new EchoCirclet());
        circlet.setAttachedTo(skyscout.getId());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        island.tap();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        harness.handlePermanentChosen(player2, island.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(island.isTapped()).isFalse();
    }
}
