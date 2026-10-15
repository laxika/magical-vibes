package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EchoTracer.class, FugitiveWizard.class})
class EchoTracerTest extends BaseCardTest {

    @Test
    void castingFaceUpDoesNotReturnACreature() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new EchoTracer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Echo Tracer");
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsCreatureToItsOwnerRatherThanItsController() {
        FugitiveWizard card = new FugitiveWizard();
        card.setOwnerId(player1.getId());
        Permanent wizard = harness.addToBattlefieldAndReturn(player2, card);
        harness.setHand(player1, List.of(new EchoTracer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent echoTracer = findPermanent(player1, "Echo Tracer");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(echoTracer));
        harness.handlePermanentChosen(player1, wizard.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInHand(player1, "Fugitive Wizard");
        harness.assertNotInHand(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Echo Tracer");
    }

    @Test
    void turningFaceUpReturnsTargetCreatureToItsOwnersHand() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new EchoTracer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent echoTracer = findPermanent(player1, "Echo Tracer");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(echoTracer));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Fugitive Wizard"));
        harness.passBothPriorities();

        assertThat(echoTracer.isFaceDown()).isFalse();
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInHand(player2, "Fugitive Wizard");
    }

    @Test
    void turningFaceUpCanReturnEchoTracerItself() {
        harness.setHand(player1, List.of(new EchoTracer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent echoTracer = findPermanent(player1, "Echo Tracer");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(echoTracer));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(echoTracer.getId());
        harness.handlePermanentChosen(player1, echoTracer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Echo Tracer");
        harness.assertInHand(player1, "Echo Tracer");
    }
}
