package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarvinMurderousMimic;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SayItsName;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnableToScream;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinterMisanthropicGuide.class, GrizzlyBears.class, Forest.class, Shock.class, Pacifism.class,
        MarvinMurderousMimic.class, SayItsName.class, UnableToScream.class})
class WinterMisanthropicGuideTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of upkeep, each player draws two cards")
    void eachPlayerDrawsTwoCardsOnUpkeep() {
        harness.addToBattlefield(player1, new WinterMisanthropicGuide());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Delirium sets each opponent's maximum hand size to seven minus graveyard card types")
    void deliriumReducesOpponentsMaximumHandSize() {
        harness.addToBattlefield(player1, new WinterMisanthropicGuide());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Without delirium, opponents keep the normal maximum hand size")
    void noDeliriumKeepsNormalMaximumHandSize() {
        harness.addToBattlefield(player1, new WinterMisanthropicGuide());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(1);
    }

    @Test
    void doesNotDrawOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new WinterMisanthropicGuide());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        gd.turnNumber = 2;
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void multipleTypesOnOneCardEnableDelirium() {
        harness.addToBattlefield(player1, new WinterMisanthropicGuide());
        harness.setGraveyard(player1, List.of(
                new MarvinMurderousMimic(), new Forest(), new UnableToScream()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(1);
    }

    @Test
    void fiveDistinctTypesReduceMaximumHandSizeToTwo() {
        harness.addToBattlefield(player1, new WinterMisanthropicGuide());
        harness.setGraveyard(player1, List.of(new MarvinMurderousMimic(), new Forest(),
                new UnableToScream(), new SayItsName(), new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void deliriumDoesNotReduceControllersHandSize() {
        harness.addToBattlefield(player1, new WinterMisanthropicGuide());
        harness.setGraveyard(player1, List.of(
                new MarvinMurderousMimic(), new Forest(), new UnableToScream()));
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void opponentsGraveyardDoesNotEnableDelirium() {
        harness.addToBattlefield(player1, new WinterMisanthropicGuide());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(
                new MarvinMurderousMimic(), new Forest(), new UnableToScream()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
    }

    @Test
    void losingAbilitiesStopsReducingOpponentsHandSize() {
        Permanent winter = harness.addToBattlefieldAndReturn(player1, new WinterMisanthropicGuide());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnableToScream());
        aura.setAttachedTo(winter.getId());
        harness.setGraveyard(player1, List.of(
                new MarvinMurderousMimic(), new Forest(), new UnableToScream()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
    }

    @Test
    void losingAbilitiesStopsUpkeepDrawTrigger() {
        Permanent winter = harness.addToBattlefieldAndReturn(player1, new WinterMisanthropicGuide());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnableToScream());
        aura.setAttachedTo(winter.getId());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void losingDeliriumRestoresNormalHandSize() {
        harness.addToBattlefield(player1, new WinterMisanthropicGuide());
        harness.setGraveyard(player1, List.of(
                new MarvinMurderousMimic(), new Forest(), new UnableToScream()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 0);

        harness.setGraveyard(player1, List.of(new MarvinMurderousMimic(), new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
    }

    @Test
    void wardCountersOpponentsSpellWhenTheyCannotPay() {
        Permanent winter = harness.addToBattlefieldAndReturn(player1, new WinterMisanthropicGuide());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, winter.getId());
        resolveAllTriggers();

        assertThat(winter.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingTwoForWardAllowsOpponentsSpellToResolve() {
        Permanent winter = harness.addToBattlefieldAndReturn(player1, new WinterMisanthropicGuide());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, winter.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(winter.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void controllersSpellDoesNotTriggerWard() {
        Permanent winter = harness.addToBattlefieldAndReturn(player1, new WinterMisanthropicGuide());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, winter.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(winter.getMarkedDamage()).isEqualTo(2);
    }
}
