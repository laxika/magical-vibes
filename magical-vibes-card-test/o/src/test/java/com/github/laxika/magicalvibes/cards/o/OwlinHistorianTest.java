package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JundCharm;
import com.github.laxika.magicalvibes.cards.n.NaturesSpiral;
import com.github.laxika.magicalvibes.cards.n.NayaCharm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OwlinHistorian.class, GrizzlyBears.class, NaturesSpiral.class, NayaCharm.class, JundCharm.class})
class OwlinHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.setHand(player1, List.of(new OwlinHistorian()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Gets +1/+1 until end of turn when a card leaves its controller's graveyard")
    void boostsWhenOwnGraveyardCardLeaves() {
        Permanent historian = addCreatureReady(player1, new OwlinHistorian());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, card.getId());
        harness.passBothPriorities();

        assertThat(historian.getPowerModifier()).isEqualTo(1);
        assertThat(historian.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a card leaves an opponent's graveyard")
    void ignoresOpponentGraveyard() {
        Permanent historian = addCreatureReady(player1, new OwlinHistorian());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new NayaCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, card.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(card);
        assertThat(historian.getPowerModifier()).isZero();
        assertThat(historian.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Surveil can keep the top card without boosting the historian")
    void surveilCanKeepTopCard() {
        Card topCard = new OwlinHistorian();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new OwlinHistorian()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        Permanent historian = findPermanent(player1, "Owlin Historian");
        assertThat(historian.getPowerModifier()).isZero();
        assertThat(historian.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Surveil with an empty library completes without a choice or a boost")
    void surveilEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new OwlinHistorian()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        Permanent historian = findPermanent(player1, "Owlin Historian");
        assertThat(historian.getPowerModifier()).isZero();
        assertThat(historian.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Exiling multiple cards together gives one boost that expires at end of turn")
    void simultaneousGraveyardDepartureTriggersOnce() {
        Permanent historian = addCreatureReady(player1, new OwlinHistorian());
        Card first = new NayaCharm();
        Card second = new JundCharm();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new JundCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(historian.getPowerModifier()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        resolveAllTriggers();

        assertThat(historian.getPowerModifier()).isEqualTo(1);
        assertThat(historian.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(historian.getPowerModifier()).isZero();
        assertThat(historian.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Separate graveyard departures in the same turn each give a boost")
    void separateGraveyardDeparturesAccumulate() {
        Permanent historian = addCreatureReady(player1, new OwlinHistorian());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new NaturesSpiral(), new NaturesSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        resolveAllTriggers();
        harness.castAndResolveSorcery(player1, 0, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(historian.getPowerModifier()).isEqualTo(2);
        assertThat(historian.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiling an empty graveyard does not trigger a boost")
    void emptyGraveyardDoesNotTrigger() {
        Permanent historian = addCreatureReady(player1, new OwlinHistorian());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new JundCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(historian.getPowerModifier()).isZero();
        assertThat(historian.getToughnessModifier()).isZero();
    }
}
