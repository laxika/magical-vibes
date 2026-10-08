package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindriddlePalaces.class, Forest.class, GrizzlyBears.class})
class WindriddlePalacesTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new WindriddlePalaces(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void revealsBothLibrariesToAllPlayers() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("revealedLibraryTopCards")
                        && message.contains("Forest")
                        && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("revealedLibraryTopCards")
                        && message.contains("Forest")
                        && message.contains("Grizzly Bears"));
    }

    @Test
    void castsFromAnotherPlayersLibraryUsingNormalMana() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    void chaosMakesEachPlayerMillOne() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void offersBothPlayersTopCardsWhenBothLibrariesAreNonempty() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.clearMessages();

        harness.publishState();

        var mapper = JsonMapper.builder().build();
        assertThat(harness.getConn1().getSentMessages()).anyMatch(message -> {
            var playable = mapper.readTree(message).get("playableLibraryTopCards");
            return playable != null && playable.toString().contains("Forest")
                    && playable.toString().contains("Grizzly Bears");
        });
    }

    @Test
    void playsOpponentsLandAndUsesNormalLandAllowance() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(first, second));

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
    }

    @Test
    void castsOwnTopCardWhenOpponentsLibraryIsEmpty() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void landCannotBePlayedOutsideMainPhase() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(forest));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void newlyExposedTopCardIsRevealedToBothPlayers() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFromLibraryTop(player1);
        harness.clearMessages();
        harness.publishState();

        var mapper = JsonMapper.builder().build();
        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            assertThat(connection.getSentMessages()).anyMatch(message -> {
                var revealed = mapper.readTree(message).get("revealedLibraryTopCards");
                return revealed != null && revealed.toString().contains("Forest")
                        && !revealed.toString().contains("Grizzly Bears");
            });
        }
    }

    @Test
    void creatureCastingStillRequiresMainPhaseAndEnoughMana() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(bears));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void leavingPlaneRemovesLibraryCastingPermission() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        gd.planechase.faceUp.clear();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void chaosMillsOnlyTopCardAndHandlesEmptyLibrary() {
        Forest top = new Forest();
        GrizzlyBears next = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(top, next));

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(next);
    }
}
