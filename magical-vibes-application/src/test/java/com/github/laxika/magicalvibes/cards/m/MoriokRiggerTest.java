package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MoriokRiggerTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact going to a graveyard offers a +1/+1 counter")
    void artifactGoingToGraveyardOffersCounter() {
        Permanent rigger = addRigger();
        harness.addToBattlefield(player2, new MindStone());

        destroyArtifact(player2, "Mind Stone");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(rigger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger does not put on a counter")
    void decliningTriggerDoesNotPutCounter() {
        Permanent rigger = addRigger();
        harness.addToBattlefield(player2, new MindStone());

        destroyArtifact(player2, "Mind Stone");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(rigger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("It triggers for an artifact controlled by Moriok Rigger's controller")
    void triggersForOwnArtifact() {
        Permanent rigger = addRigger();
        harness.addToBattlefield(player1, new MindStone());

        destroyArtifact(player1, "Mind Stone");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(rigger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-artifact going to a graveyard does not trigger it")
    void nonArtifactGoingToGraveyardDoesNotTrigger() {
        Permanent rigger = addRigger();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(rigger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addRigger() {
        return harness.addToBattlefieldAndReturn(player1, new MoriokRigger());
    }

    private void destroyArtifact(com.github.laxika.magicalvibes.model.Player artifactController, String name) {
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID artifactId = harness.getPermanentId(artifactController, name);
        harness.castInstant(player1, 0, artifactId);
    }
}
