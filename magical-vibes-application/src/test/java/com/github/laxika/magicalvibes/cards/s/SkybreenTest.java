package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skybreen.class, Forest.class, GrizzlyBears.class, Shock.class, Ornithopter.class,
        HowlingMine.class})
class SkybreenTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Skybreen(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void revealsTheTopCardOfEachLibraryToAllPlayers() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Shock()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("revealedLibraryTopCards")
                        && message.contains("Grizzly Bears")
                        && message.contains("Shock"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("revealedLibraryTopCards")
                        && message.contains("Grizzly Bears")
                        && message.contains("Shock"));
    }

    @Test
    void preventsCastingSpellsThatShareATypeWithAnyLibraryTopCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allowsPlayingLandsAndCastingSpellsWithNoSharedType() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void chaosMakesTargetPlayerLoseTheirHandSizeInLife() {
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Shock()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void emptyLibrariesDoNotRestrictCasting() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void restrictionTracksChangesToLibraryTopCards() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setLibrary(player2, List.of(new Forest()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void creatureTypeOfArtifactCreatureOnTopPreventsCreatureSpells() {
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactCreatureSpellIsBlockedByCreatureCardOnTop() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Ornithopter()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void leavingSkybreenRemovesCastingRestrictionAndPublicRevelation() {
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        gd.planechase.faceUp.clear();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).noneMatch(message ->
                message.contains("Grizzly Bears") || message.contains("\"name\":\"Shock\""));
        assertThat(harness.getConn2().getSentMessages()).noneMatch(message ->
                message.contains("Grizzly Bears") || message.contains("\"name\":\"Shock\""));

        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
    }

    @Test
    void artifactTypeOfArtifactCreatureOnTopPreventsNoncreatureArtifacts() {
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new HowlingMine()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactCreatureSpellIsBlockedByNoncreatureArtifactOnTop() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new HowlingMine()));
        harness.setHand(player1, List.of(new Ornithopter()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chaosUsesHandSizeAtResolutionAfterTargetCastsInResponse() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void chaosCanTargetControllerWithAnEmptyHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
