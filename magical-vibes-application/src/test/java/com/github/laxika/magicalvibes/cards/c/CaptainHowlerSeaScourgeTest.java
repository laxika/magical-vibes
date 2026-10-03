package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DangerousWager;
import com.github.laxika.magicalvibes.cards.b.BounceOff;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainHowlerSeaScourge.class, DangerousWager.class, GrizzlyBears.class, Peek.class, Forest.class, BounceOff.class})
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
        declareAttackersAndPrepareBlockers(player1, List.of(1));
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
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
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

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay the mana cost")
    void unpaidWardCountersOpponentSpell() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainHowlerSeaScourge());
        harness.setHand(player2, List.of(new BounceOff()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, captain.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
        harness.assertInGraveyard(player2, "Bounce Off");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Watching an opponent's creature draws for Captain Howler's controller")
    void opponentCreatureDrawsForTriggerController() {
        harness.addToBattlefield(player1, new CaptainHowlerSeaScourge());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(2);
        int controllerHand = gd.playerHands.get(player1.getId()).size();
        int opponentHand = gd.playerHands.get(player2.getId()).size();
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHand + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHand);
    }

    @Test
    @DisplayName("Separate discard events create cumulative boosts and separate draw triggers")
    void repeatedDiscardsCreateSeparateDrawTriggers() {
        harness.addToBattlefield(player1, new CaptainHowlerSeaScourge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        for (int i = 0; i < 2; i++) {
            harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.castInstant(player1, 0);
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, bears.getId());
            harness.passBothPriorities();
        }

        assertThat(bears.getPowerModifier()).isEqualTo(4);
        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(1));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCombat + 2);
    }
}
