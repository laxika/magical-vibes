package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WordOfSeizing;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({Goldmeadow.class, Forest.class, WordOfSeizing.class})
class GoldmeadowTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Goldmeadow(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void ownLandCreatesThreeGoatsForItsController() {
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).hasSize(3);
        assertThat(findPermanents(player2, "Goat")).isEmpty();
    }

    @Test
    void opponentsLandCreatesThreeGoatsForThatOpponent() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).isEmpty();
        assertThat(findPermanents(player2, "Goat")).hasSize(3);
    }

    @Test
    void chaosCreatesOneGoatForPlanarController() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).hasSize(1);
        assertThat(findPermanents(player2, "Goat")).isEmpty();
    }

    @Test
    void landEnteringWithoutBeingPlayedCreatesThreeGoats() {
        harness.enterBattlefieldAndReturn(player2, new Forest());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).isEmpty();
        assertThat(findPermanents(player2, "Goat")).hasSize(3).allSatisfy(goat -> {
            assertThat(goat.getCard().isToken()).isTrue();
            assertThat(goat.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(goat.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(goat.getCard().getSubtypes()).containsExactly(CardSubtype.GOAT);
            assertThat(goat.getCard().getPower()).isZero();
            assertThat(goat.getCard().getToughness()).isEqualTo(1);
            assertThat(goat.isTapped()).isFalse();
        });
    }

    @Test
    void landTriggerUsesCurrentLandControllerAtResolution() {
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        var land = findPermanent(player1, "Forest");
        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0, land.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).isEmpty();
        assertThat(findPermanents(player2, "Goat")).hasSize(3);
    }

    @Test
    void landTriggerStillResolvesAfterPlaneLeaves() {
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        gd.planechase.faceUp.clear();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).hasSize(3);
        assertThat(findPermanents(player2, "Goat")).isEmpty();
    }

    @Test
    void chaosCreatesGoatForOpponentWhenOpponentControlsPlane() {
        harness.forceActivePlayer(player2);
        gd.planechase.controllerId = player2.getId();

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).isEmpty();
        assertThat(findPermanents(player2, "Goat")).hasSize(1);
    }

    @Test
    void chaosTriggerStillResolvesAfterPlaneLeaves() {
        harness.inMutationScope(() -> planar.chaos(gd));

        gd.planechase.faceUp.clear();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goat")).hasSize(1);
        assertThat(findPermanents(player2, "Goat")).isEmpty();
    }
}
