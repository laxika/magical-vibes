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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
