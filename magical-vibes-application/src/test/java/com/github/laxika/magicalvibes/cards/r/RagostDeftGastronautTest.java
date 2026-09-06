package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RagostDeftGastronaut.class, FountainOfYouth.class, Spellbook.class})
class RagostDeftGastronautTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts you control become Foods with the Food sacrifice ability")
    void grantsFoodAbilityToOwnArtifacts() {
        Permanent ragost = addRagost();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        goToMainPhase();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, indexOf(player1, ownArtifact), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingArtifact);
        assertThat(ragost.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a Food deals 3 damage to each opponent")
    void sacrificesFoodToDamageOpponents() {
        Permanent ragost = addRagost();
        Permanent food = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        goToMainPhase();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, ragost), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(ragost.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps at your end step after you gain life")
    void untapsAtEndStepAfterLifeGain() {
        Permanent ragost = addRagost();
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        goToMainPhase();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, fountain), 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, ragost), 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fountain);
        assertThat(ragost.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(() -> stepTriggerService().handleEndStepTriggers(gd));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ragost.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap at the end step when you did not gain life")
    void doesNotUntapAtEndStepWithoutLifeGain() {
        Permanent ragost = addRagost();
        harness.addToBattlefieldAndReturn(player1, new Spellbook());
        goToMainPhase();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(player1, ragost), 0, null, null);
        harness.passBothPriorities();
        assertThat(ragost.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(() -> stepTriggerService().handleEndStepTriggers(gd));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ragost.isTapped()).isTrue();
    }

    private Permanent addRagost() {
        return addCreatureReady(player1, new RagostDeftGastronaut());
    }

    private void goToMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private StepTriggerService stepTriggerService() {
        return GameTestEngineContext.get().getBean(StepTriggerService.class);
    }
}
