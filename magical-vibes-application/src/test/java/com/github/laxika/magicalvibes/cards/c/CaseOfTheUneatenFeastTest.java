package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseOfTheUneatenFeast.class, SavannahLions.class, WalkingCorpse.class})
class CaseOfTheUneatenFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Gains one life whenever a creature enters under your control")
    void gainsLifeWhenCreatureEnters() {
        harness.addToBattlefield(player1, new CaseOfTheUneatenFeast());
        harness.setHand(player1, List.of(new SavannahLions()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Solves at the beginning of the end step after gaining five life")
    void solvesAfterGainingFiveLife() {
        Permanent feast = harness.addToBattlefieldAndReturn(player1, new CaseOfTheUneatenFeast());
        gainFiveLifeFromCreatureEntries();

        resolveEndStepTriggers();

        assertThat(feast.isSolved()).isTrue();
    }

    @Test
    @DisplayName("The solved ability sacrifices the Case and allows a creature to be cast from the graveyard")
    void solvedAbilityAllowsCreatureGraveyardCast() {
        gainFiveLifeAndSolveCase();

        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Corpse");
        assertThat(findPermanents(player1, "Case of the Uneaten Feast")).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while the Case is unsolved")
    void cannotActivateWhileUnsolved() {
        harness.addToBattlefield(player1, new CaseOfTheUneatenFeast());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("solved");
    }

    @Test
    void doesNotGainLifeForOpponentsCreature() {
        harness.addToBattlefield(player1, new CaseOfTheUneatenFeast());
        harness.setHand(player2, List.of(new SavannahLions()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotSolveAfterOnlyFourLifeGained() {
        Permanent feast = harness.addToBattlefieldAndReturn(player1, new CaseOfTheUneatenFeast());
        harness.setHand(player1, List.of(new SavannahLions(), new SavannahLions(),
                new SavannahLions(), new SavannahLions()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        for (int i = 0; i < 4; i++) {
            harness.castCreature(player1, 0);
            resolveAllTriggers();
        }

        resolveEndStepTriggers();

        assertThat(feast.isSolved()).isFalse();
    }

    @Test
    void doesNotSolveDuringOpponentsEndStep() {
        Permanent feast = harness.addToBattlefieldAndReturn(player1, new CaseOfTheUneatenFeast());
        gainFiveLifeFromCreatureEntries();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(feast.isSolved()).isFalse();
    }

    @Test
    void solvedAbilityDoesNotPermitNoncreatureCards() {
        gainFiveLifeAndSolveCase();
        harness.setGraveyard(player1, List.of(new CaseOfTheUneatenFeast()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureEnteringGraveyardAfterResolutionDoesNotGainCastingAbility() {
        gainFiveLifeAndSolveCase();
        harness.setGraveyard(player1, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardPermissionDoesNotWaiveManaCost() {
        gainFiveLifeAndSolveCase();
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    void graveyardPermissionDoesNotWaiveCreatureTiming() {
        gainFiveLifeAndSolveCase();
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    private void gainFiveLifeAndSolveCase() {
        harness.addToBattlefield(player1, new CaseOfTheUneatenFeast());
        gainFiveLifeFromCreatureEntries();
        resolveEndStepTriggers();
    }

    private void gainFiveLifeFromCreatureEntries() {
        harness.setHand(player1, List.of(
                new SavannahLions(),
                new SavannahLions(),
                new SavannahLions(),
                new SavannahLions(),
                new SavannahLions()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        for (int i = 0; i < 5; i++) {
            harness.castCreature(player1, 0);
            resolveAllTriggers();
        }
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
