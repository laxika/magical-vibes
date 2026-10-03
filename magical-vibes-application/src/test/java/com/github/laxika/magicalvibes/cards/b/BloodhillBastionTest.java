package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodhillBastion.class, GrizzlyBears.class})
class BloodhillBastionTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new BloodhillBastion(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void creaturesEnteringUnderEitherPlayersControlGainDoubleStrikeAndHaste() {
        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isTrue();
    }

    @Test
    void chaosFlickersTargetNontokenCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent illegalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellTargetTrigger(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, illegalTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    void planeControlsTriggerWhenOpponentsCreatureEnters() {
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BloodhillBastion.class);
    }

    @Test
    void grantedKeywordsExpireAtEndOfTurn() {
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void chaosRejectsTokenAndReturnedCreatureGainsKeywords() {
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextSpellTargetTrigger(gd));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, token.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(token.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }
}
