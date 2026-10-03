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

@CardUsed({CephalidFacetaker.class, GrizzlyBears.class, Clone.class})
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
        harness.passUntil(player1, TurnStep.CLEANUP);

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

    @Test
    @DisplayName("Does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        addCreatureReady(player1, new CephalidFacetaker());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("No copy choice is offered when there is no other creature")
    void noOtherCreature() {
        Permanent facetaker = addCreatureReady(player1, new CephalidFacetaker());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, facetaker)).isTrue();
    }

    @Test
    @DisplayName("A subsequent copy inherits the unblockable exception")
    void unblockableExceptionIsCopiable() {
        Permanent first = addCreatureReady(player1, new CephalidFacetaker());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, first.getId());
        resolveAllTriggers();

        Permanent second = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof Clone)
                .findFirst().orElseThrow();

        assertThat(second.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasCantBeBlocked(gd, second)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(first.getCard().getName()).isEqualTo("Cephalid Facetaker");
        assertThat(second.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasCantBeBlocked(gd, second)).isTrue();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
