package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KimahriValiantGuardian.class, GrizzlyBears.class, ProdigalPyromancer.class})
class KimahriValiantGuardianTest extends BaseCardTest {

    @Test
    void targetsOnlyCreatureAnOpponentControls() {
        Permanent kimahri = addCreatureReady(player1, new KimahriValiantGuardian());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opposingBear.getId())
                .doesNotContain(kimahri.getId(), ownBear.getId());
    }

    @Test
    void countersAndTapsBeforeOptionalCopy() {
        Permanent kimahri = addCreatureReady(player1, new KimahriValiantGuardian());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, opposingBear.getId());
        harness.passBothPriorities();

        assertThat(kimahri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBear.isTapped()).isTrue();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(kimahri.getCard().getName()).isEqualTo("Kimahri, Valiant Guardian");
        assertThat(gqs.getEffectivePower(gd, kimahri)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kimahri)).isEqualTo(4);
    }

    @Test
    void acceptedCopyKeepsNameVigilanceAndRonsoRage() {
        Permanent kimahri = addCreatureReady(player1, new KimahriValiantGuardian());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kimahri.getCard().getName()).isEqualTo("Kimahri, Valiant Guardian");
        assertThat(gqs.getEffectivePower(gd, kimahri)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kimahri)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, kimahri, Keyword.VIGILANCE)).isTrue();

        Permanent secondTarget = addCreatureReady(player2, new ProdigalPyromancer());
        advanceToBeginningOfCombat(player1);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(secondTarget.getId());

        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kimahri.getCard().getName()).isEqualTo("Kimahri, Valiant Guardian");
        assertThat(kimahri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kimahri, Keyword.VIGILANCE)).isTrue();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleBeginningOfCombatTriggers(gd));
    }
}
