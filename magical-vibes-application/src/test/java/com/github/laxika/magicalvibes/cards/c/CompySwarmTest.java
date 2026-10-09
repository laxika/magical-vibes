package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CompySwarm.class})
class CompySwarmTest extends BaseCardTest {

    @Test
    void doesNotCreateACopyWhenNoCreatureDied() {
        harness.addToBattlefield(player1, new CompySwarm());

        resolveControllerEndStep();

        assertThat(tokenCopies()).isEmpty();
    }

    @Test
    void createsOneTappedCopyWhenACreatureDied() {
        harness.addToBattlefield(player1, new CompySwarm());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        resolveControllerEndStep();

        assertThat(tokenCopies()).hasSize(1);
        assertThat(tokenCopies().getFirst().isTapped()).isTrue();
    }

    @Test
    void tokenCopyPreservesBothColors() {
        harness.addToBattlefield(player1, new CompySwarm());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        resolveControllerEndStep();

        assertThat(tokenCopies()).hasSize(1);
        assertThat(harness.getGameQueryService().getEffectiveColors(gd, tokenCopies().getFirst()))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new CompySwarm());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(tokenCopies()).isEmpty();
    }

    @Test
    void deathAfterEndStepBeginsDoesNotTriggerRetroactively() {
        harness.addToBattlefield(player1, new CompySwarm());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new CompySwarm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, victim));

        assertThat(gd.stack).isEmpty();
        assertThat(tokenCopies()).isEmpty();
    }

    @Test
    void multipleCreatureDeathsStillCreateOnlyOneCopy() {
        harness.addToBattlefield(player1, new CompySwarm());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CompySwarm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CompySwarm());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, second);
        });

        resolveControllerEndStep();

        assertThat(tokenCopies()).hasSize(1);
        assertThat(tokenCopies().getFirst().isTapped()).isTrue();
    }

    @Test
    void createsCopyEvenWhenSourceDiesInResponse() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CompySwarm());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        assertThat(tokenCopies()).hasSize(1);
        assertThat(tokenCopies().getFirst().isTapped()).isTrue();
    }

    @Test
    void tokenCopyRetainsAbilityAndDoesNotTriggerInItsCreationStep() {
        harness.addToBattlefield(player1, new CompySwarm());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        resolveControllerEndStep();
        assertThat(tokenCopies()).hasSize(1);
        assertThat(gd.stack).isEmpty();

        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tokenCopies()).hasSize(3);
        assertThat(tokenCopies()).allMatch(Permanent::isTapped);
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private java.util.List<Permanent> tokenCopies() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
