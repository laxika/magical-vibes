package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HoppingAutomaton;
import com.github.laxika.magicalvibes.cards.w.Whetstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Turnabout.class, ArgothianSwine.class, Forest.class, HoppingAutomaton.class, Whetstone.class})
class TurnaboutTest extends BaseCardTest {

    @Test
    @DisplayName("Taps only untapped creatures controlled by the target player")
    void tapsCreatures() {
        Permanent untappedCreature = addCreatureReady(player2, new ArgothianSwine());
        Permanent tappedCreature = addCreatureReady(player2, new ArgothianSwine());
        tappedCreature.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Whetstone());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castTurnabout(player2.getId());
        harness.handleListChoice(player1, "TAP_CREATURE");

        assertThat(untappedCreature.isTapped()).isTrue();
        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps only tapped lands controlled by the target player")
    void untapsLands() {
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        tappedLand.tap();
        Permanent untappedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = addCreatureReady(player2, new ArgothianSwine());
        creature.tap();

        castTurnabout(player2.getId());
        harness.handleListChoice(player1, "UNTAP_LAND");

        assertThat(tappedLand.isTapped()).isFalse();
        assertThat(untappedLand.isTapped()).isFalse();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can choose artifacts and does not affect permanents of the other player")
    void tapsArtifactsOnlyOnTargetPlayer() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Whetstone());
        Permanent artifactCreature = addCreatureReady(player2, new HoppingAutomaton());
        Permanent creature = addCreatureReady(player2, new ArgothianSwine());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Whetstone());

        castTurnabout(player2.getId());
        harness.handleListChoice(player1, "TAP_ARTIFACT");

        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifactCreature.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
        assertThat(ownArtifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps only untapped lands controlled by the target player")
    void tapsLands() {
        Permanent untappedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        tappedLand.tap();
        Permanent creature = addCreatureReady(player2, new ArgothianSwine());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        castTurnabout(player2.getId());
        harness.handleListChoice(player1, "TAP_LAND");

        assertThat(untappedLand.isTapped()).isTrue();
        assertThat(tappedLand.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps only tapped creatures controlled by the target player")
    void untapsCreatures() {
        Permanent tappedCreature = addCreatureReady(player2, new ArgothianSwine());
        tappedCreature.tap();
        Permanent untappedCreature = addCreatureReady(player2, new ArgothianSwine());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.tap();

        castTurnabout(player2.getId());
        harness.handleListChoice(player1, "UNTAP_CREATURE");

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(untappedCreature.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps only tapped artifacts controlled by the target player")
    void untapsArtifacts() {
        Permanent tappedArtifact = harness.addToBattlefieldAndReturn(player2, new Whetstone());
        tappedArtifact.tap();
        Permanent untappedArtifact = harness.addToBattlefieldAndReturn(player2, new Whetstone());
        Permanent creature = addCreatureReady(player2, new ArgothianSwine());
        creature.tap();

        castTurnabout(player2.getId());
        harness.handleListChoice(player1, "UNTAP_ARTIFACT");

        assertThat(tappedArtifact.isTapped()).isFalse();
        assertThat(untappedArtifact.isTapped()).isFalse();
        assertThat(creature.isTapped()).isTrue();
    }

    private void castTurnabout(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Turnabout()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
