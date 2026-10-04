package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IAmDuskmourn.class, SolRing.class})
class IAmDuskmournTest extends BaseCardTest {

    @Test
    void mayCastSpellFromHandForFreeThenAbandonsScheme() {
        Permanent scheme = addScheme();
        harness.setHand(player1, List.of(new SolRing()));

        resolveControllerEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme.getCard());
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    void decliningFreeCastKeepsSchemeAndCardInHand() {
        Permanent scheme = addScheme();
        SolRing ring = new SolRing();
        harness.setHand(player1, List.of(ring));

        resolveControllerEndStep();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ring);
    }

    @Test
    void abandonsSchemeBeforePlayersCanRespondToFreeSpell() {
        Permanent scheme = addScheme();
        SolRing ring = new SolRing();
        harness.setHand(player1, List.of(ring));

        resolveControllerEndStep();

        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.stack).extracting(entry -> entry.getCard()).containsExactly(ring);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyHandKeepsSchemeFaceUp() {
        Permanent scheme = addScheme();
        harness.setHand(player1, List.of());

        resolveControllerEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotOfferFreeSpellDuringOpponentsEndStep() {
        Permanent scheme = addScheme();
        SolRing ring = new SolRing();
        harness.setHand(player1, List.of(ring));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ring);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingOneSpellDoesNotOfferAnotherSpell() {
        addScheme();
        SolRing first = new SolRing();
        SolRing second = new SolRing();
        harness.setHand(player1, List.of(first, second));

        resolveControllerEndStep();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(countPermanents(player1, "Sol Ring")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addScheme() {
        return harness.addToBattlefieldAndReturn(player1, new IAmDuskmourn());
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
