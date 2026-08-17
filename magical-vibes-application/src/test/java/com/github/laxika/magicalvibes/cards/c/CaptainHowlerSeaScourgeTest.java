package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DangerousWager;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaptainHowlerSeaScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("a multi-card discard pumps the chosen creature and watches it for combat damage")
    void discardEventPumpsAndWatchesChosenCreature() {
        harness.addToBattlefield(player1, new CaptainHowlerSeaScourge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears(), new Peek()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Peek()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DiscardControllerTriggerTarget.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(4);
        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(1));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBeforeCombat + 1);
    }

    @Test
    @DisplayName("the delayed trigger does not fire when the creature only deals combat damage to a creature")
    void combatDamageToCreatureDoesNotDraw() {
        harness.addToBattlefield(player1, new CaptainHowlerSeaScourge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears(), new Peek()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Peek()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBeforeCombat);
    }

    @Test
    @DisplayName("the delayed trigger expires at end of turn")
    void delayedTriggerExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new CaptainHowlerSeaScourge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears(), new Peek()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Peek()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(1));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBeforeCombat);
    }

    @Test
    @DisplayName("the discard trigger only accepts a creature target")
    void discardTriggerRejectsNonCreatureTarget() {
        harness.addToBattlefield(player1, new CaptainHowlerSeaScourge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new com.github.laxika.magicalvibes.cards.f.Forest());
        harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears(), new Peek()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Peek()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(bears.getId()).doesNotContain(land.getId());
        assertThat(bears.getPowerModifier()).isEqualTo(0);
    }
}
