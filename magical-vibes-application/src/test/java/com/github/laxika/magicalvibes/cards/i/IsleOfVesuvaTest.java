package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

@CardUsed({IsleOfVesuva.class, GrizzlyBears.class, Forest.class, Unsummon.class})
class IsleOfVesuvaTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new IsleOfVesuva(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void nontokenCreatureEnteringUnderEitherPlayersControlCreatesOneCopyForItsController() {
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(
                permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenCopyDoesNotTriggerTheAbilityAgain() {
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noncreatureEnteringDoesNotCreateACopy() {
        Permanent forest = harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(forest);
    }

    @Test
    void chaosDestroysTheOriginalAndItsTokenCopy() {
        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(2);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void copyIsCreatedUsingLastKnownInformationWhenEnteringCreatureLeaves() {
        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement().satisfies(copy -> {
            assertThat(copy.getCard().isToken()).isTrue();
            assertThat(copy.getCard().getName()).isEqualTo("Grizzly Bears");
        });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chaosDoesNotDestroyOtherCreaturesWhenItsTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(other);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chaosDestroysTargetAndAllOtherCreaturesWithTheSameName() {
        Permanent player1Bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player1Bears.getId(), player2Bears.getId())
                .doesNotContain(forest.getId());

        harness.handlePermanentChosen(player1, player1Bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(forest);
    }

}
