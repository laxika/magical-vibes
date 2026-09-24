package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.d.DevourInShadow;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.t.TelJiladJustice;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoriokRigger.class, ConjurersBauble.class, DrossCrocodile.class,
        DevourInShadow.class, TelJiladJustice.class})
class MoriokRiggerTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact going to a graveyard offers a +1/+1 counter")
    void artifactGoingToGraveyardOffersCounter() {
        Permanent rigger = addRigger();
        harness.addToBattlefield(player2, new ConjurersBauble());

        resolveArtifactTrigger(player2, "Conjurer's Bauble", true);

        assertThat(rigger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger does not put on a counter")
    void decliningTriggerDoesNotPutCounter() {
        Permanent rigger = addRigger();
        harness.addToBattlefield(player2, new ConjurersBauble());

        resolveArtifactTrigger(player2, "Conjurer's Bauble", false);

        assertThat(rigger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("It triggers for an artifact controlled by Moriok Rigger's controller")
    void triggersForOwnArtifact() {
        Permanent rigger = addRigger();
        harness.addToBattlefield(player1, new ConjurersBauble());

        resolveArtifactTrigger(player1, "Conjurer's Bauble", true);

        assertThat(rigger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each artifact going to a graveyard creates a separate trigger")
    void eachArtifactCreatesSeparateTrigger() {
        Permanent rigger = addRigger();
        harness.addToBattlefield(player2, new ConjurersBauble());
        harness.addToBattlefield(player2, new ConjurersBauble());

        resolveArtifactTrigger(player2, "Conjurer's Bauble", true);
        resolveArtifactTrigger(player2, "Conjurer's Bauble", true);

        assertThat(rigger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-artifact going to a graveyard does not trigger it")
    void nonArtifactGoingToGraveyardDoesNotTrigger() {
        Permanent rigger = addRigger();
        harness.addToBattlefield(player2, new DrossCrocodile());

        harness.setHand(player1, List.of(new DevourInShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        UUID creatureId = harness.getPermanentId(player2, "Dross Crocodile");
        harness.castAndResolveInstant(player1, 0, creatureId);

        assertThat(rigger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addRigger() {
        return harness.addToBattlefieldAndReturn(player1, new MoriokRigger());
    }

    private void resolveArtifactTrigger(Player artifactController, String name, boolean accept) {
        destroyArtifact(artifactController, name);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private void destroyArtifact(Player artifactController, String name) {
        harness.setHand(player1, List.of(new TelJiladJustice()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of());
        UUID artifactId = harness.getPermanentId(artifactController, name);
        harness.castAndResolveInstant(player1, 0, artifactId);
    }
}
