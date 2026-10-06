package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlindingDrone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoxodonSmiter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RealitySmasher.class, Shock.class, GrizzlyBears.class, BlindingDrone.class, LoxodonSmiter.class})
class RealitySmasherTest extends BaseCardTest {

    @Test
    void countersAnOpponentSpellWhenItsControllerHasNoCardToDiscard() {
        Permanent smasher = addReadyRealitySmasher();
        prepareOpponentSpell(List.of(new Shock()));

        harness.castAndResolveInstant(player2, 0, smasher.getId());

        harness.assertInGraveyard(player2, "Shock");
        assertThat(smasher.getMarkedDamage()).isZero();
    }

    @Test
    void countersAnOpponentSpellWhenItsControllerDeclinesToDiscard() {
        Permanent smasher = addReadyRealitySmasher();
        prepareOpponentSpell(List.of(new Shock(), new GrizzlyBears()));

        harness.castAndResolveInstant(player2, 0, smasher.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(smasher.getMarkedDamage()).isZero();
    }

    @Test
    void anOpponentMayDiscardToLetTheirSpellResolve() {
        Permanent smasher = addReadyRealitySmasher();
        prepareOpponentSpell(List.of(new Shock(), new GrizzlyBears()));

        harness.castAndResolveInstant(player2, 0, smasher.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(smasher.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void doesNotCounterItsControllersSpell() {
        Permanent smasher = addReadyRealitySmasher();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, smasher.getId());

        assertThat(smasher.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerForAnOpponentsActivatedAbility() {
        Permanent smasher = addReadyRealitySmasher();
        Permanent drone = addCreatureReady(player2, new BlindingDrone());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, smasher.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(drone.isTapped()).isTrue();
        assertThat(smasher.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void discardingLoxodonSmiterToTheOpponentsTriggerPutsItOntoTheBattlefield() {
        Permanent smasher = addReadyRealitySmasher();
        prepareOpponentSpell(List.of(new Shock(), new LoxodonSmiter()));

        harness.castAndResolveInstant(player2, 0, smasher.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Loxodon Smiter");
        harness.assertNotInGraveyard(player2, "Loxodon Smiter");
        harness.assertNotInHand(player2, "Loxodon Smiter");
        assertThat(smasher.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addReadyRealitySmasher() {
        return harness.addToBattlefieldAndReturn(player1, new RealitySmasher());
    }

    private void prepareOpponentSpell(List<com.github.laxika.magicalvibes.model.Card> hand) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, hand);
        harness.addMana(player2, ManaColor.RED, 1);
    }
}
