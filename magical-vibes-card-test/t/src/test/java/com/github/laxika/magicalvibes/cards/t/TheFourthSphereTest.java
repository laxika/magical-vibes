package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZulaportEnforcer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFourthSphere.class, GrizzlyBears.class, ZulaportEnforcer.class})
class TheFourthSphereTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheFourthSphere(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    @Test
    void upkeepSacrificesANonblackCreature() {
        Permanent nonblackCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blackCreature = addCreatureReady(player1, new ZulaportEnforcer());

        triggerUpkeep();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonblackCreature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blackCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nonblackCreature);
    }

    @Test
    void upkeepDoesNothingWhenThereIsNoNonblackCreature() {
        Permanent blackCreature = addCreatureReady(player1, new ZulaportEnforcer());

        triggerUpkeep();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blackCreature);
    }

    @Test
    void chaosCreatesABlackZombieToken() {
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Zombie");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    private void triggerUpkeep() {
        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleUpkeepTriggers(gd));
        harness.passBothPriorities();
    }

    @Test
    void upkeepLetsControllerChooseExactlyOneNonblackCreature() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent black = addCreatureReady(player1, new ZulaportEnforcer());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        triggerUpkeep();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first, black);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponent);
    }

    @Test
    void upkeepOnOtherPlayersTurnSacrificesOnlyTheirCreature() {
        Permanent originalControllerCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent activePlayerCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        triggerUpkeep();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(activePlayerCreature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(originalControllerCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void chaosAfterControlChangesCreatesTokenForNewController() {
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        triggerUpkeep();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
    }

    @Test
    void upkeepDoesNothingOnEmptyBattlefield() {
        triggerUpkeep();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
