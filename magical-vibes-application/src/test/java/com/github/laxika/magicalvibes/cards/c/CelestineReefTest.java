package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LaboratoryManiac;
import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CelestineReef.class, GrizzlyBears.class, LaboratoryManiac.class, RiverBoa.class, SuntailHawk.class})
class CelestineReefTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new CelestineReef(), gd.nextTimestamp()));
    }

    @Test
    @DisplayName("Creatures without flying or islandwalk cannot attack")
    void groundCreatureCannotAttack() {
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying creatures can attack")
    void flyingCreatureCanAttack() {
        addCreatureReady(player2, new SuntailHawk());

        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Islandwalk creatures can attack")
    void islandwalkCreatureCanAttack() {
        addCreatureReady(player2, new RiverBoa());

        assertThatCode(() -> declareAttackers(player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Chaos prevents the controller from losing until a player planeswalks")
    void chaosPreventsControllerLossUntilPlaneswalk() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        harness.setLife(player1, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        gd.planechase.deck.add(new CelestineReef());
        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Chaos prevents an opponent from winning until a player planeswalks")
    void chaosPreventsOpponentWinUntilPlaneswalk() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        harness.addToBattlefield(player2, new LaboratoryManiac());
        gd.playerDecks.put(player2.getId(), new ArrayList<>());
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        gd.planechase.deck.add(new CelestineReef());
        harness.inMutationScope(() -> planar.planeswalk(gd));

        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
