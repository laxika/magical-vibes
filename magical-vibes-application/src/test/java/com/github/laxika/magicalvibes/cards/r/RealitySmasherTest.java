package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RealitySmasher.class, Shock.class, GrizzlyBears.class})
class RealitySmasherTest extends BaseCardTest {

    @Test
    void countersAnOpponentSpellWhenItsControllerHasNoCardToDiscard() {
        Permanent smasher = addReadyRealitySmasher();
        prepareOpponentSpell(List.of(new Shock()));

        harness.castInstant(player2, 0, smasher.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(smasher.getMarkedDamage()).isZero();
    }

    @Test
    void countersAnOpponentSpellWhenItsControllerDeclinesToDiscard() {
        Permanent smasher = addReadyRealitySmasher();
        prepareOpponentSpell(List.of(new Shock(), new GrizzlyBears()));

        harness.castInstant(player2, 0, smasher.getId());
        harness.passBothPriorities();

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

        harness.castInstant(player2, 0, smasher.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
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
