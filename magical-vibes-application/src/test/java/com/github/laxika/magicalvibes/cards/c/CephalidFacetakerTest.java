package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CephalidFacetaker.class, GrizzlyBears.class})
class CephalidFacetakerTest extends BaseCardTest {

    @Test
    @DisplayName("Cephalid Facetaker can't be blocked")
    void cantBeBlocked() {
        Permanent facetaker = addCreatureReady(player1, new CephalidFacetaker());

        assertThat(gqs.hasCantBeBlocked(gd, facetaker)).isTrue();
    }

    @Test
    @DisplayName("Beginning of combat targets another creature")
    void beginningOfCombatTargetsAnotherCreature() {
        Permanent facetaker = addCreatureReady(player1, new CephalidFacetaker());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(ownBears.getId(), opponentBears.getId())
                .doesNotContain(facetaker.getId());
    }

    @Test
    @DisplayName("Accepting the trigger creates a 1/4 unblockable copy until end of turn")
    void copiesTargetWithExceptions() {
        Permanent facetaker = addCreatureReady(player1, new CephalidFacetaker());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(facetaker.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, facetaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, facetaker)).isEqualTo(4);
        assertThat(gqs.hasCantBeBlocked(gd, facetaker)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(facetaker.getCard().getName()).isEqualTo("Cephalid Facetaker");
    }

    @Test
    @DisplayName("Declining the trigger leaves Cephalid Facetaker unchanged")
    void mayBeDeclined() {
        Permanent facetaker = addCreatureReady(player1, new CephalidFacetaker());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(facetaker.getCard().getName()).isEqualTo("Cephalid Facetaker");
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
