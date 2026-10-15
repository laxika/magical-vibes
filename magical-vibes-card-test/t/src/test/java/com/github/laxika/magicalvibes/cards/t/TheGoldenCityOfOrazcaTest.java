package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({TheGoldenCityOfOrazca.class, GrizzlyBears.class, Forest.class})
class TheGoldenCityOfOrazcaTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheGoldenCityOfOrazca(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void ascendGrantsTheCitysBlessingWhenThePlaneArrives() {
        gd.planechase.faceUp.clear();
        gd.planechase.deck.addFirst(new TheGoldenCityOfOrazca());
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        harness.inMutationScope(() -> planar.reveal(gd, false));

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    void combatDamageCreatesOneTreasureAndDrawsWithTheCitysBlessing() {
        gd.playersWithCityBlessing.add(player1.getId());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(libraryCard);
    }

    @Test
    void chaosMayPutAPermanentCardOntoTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears).isNotNull();
        assertThat(bears.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void simultaneousCombatDamageFromTwoCreaturesCreatesOnlyOneTreasure() {
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(libraryCard);
    }

    @Test
    void treasureAsTenthPermanentGrantsBlessingBeforeDrawing() {
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        addCreatureReady(player1, new GrizzlyBears());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(libraryCard);
    }

    @Test
    void planeDoesNotCountAsATenthPermanent() {
        gd.planechase.faceUp.clear();
        gd.planechase.deck.addFirst(new TheGoldenCityOfOrazca());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.inMutationScope(() -> planar.reveal(gd, false));

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }

    @Test
    void chaosCanBeDeclined() {
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void chaosCanPutALandOntoTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new Forest()));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotInHand(player1, "Forest");
    }
}
