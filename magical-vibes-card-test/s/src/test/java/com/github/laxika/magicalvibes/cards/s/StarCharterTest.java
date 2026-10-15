package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarCharter.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class StarCharterTest extends BaseCardTest {

    @Test
    @DisplayName("Offers an eligible creature after its controller gained life")
    void triggersAfterLifeGain() {
        harness.addToBattlefield(player1, new StarCharter());
        Card eligibleCreature = new LlanowarElves();
        Card secondEligibleCreature = new GrizzlyBears();
        setupTopFour(List.of(eligibleCreature, new AirElemental(), new Shock(), secondEligibleCreature));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).validCardIds())
                .containsExactlyInAnyOrder(eligibleCreature.getId(), secondEligibleCreature.getId());
    }

    @Test
    @DisplayName("Puts a chosen creature into hand after its controller lost life")
    void triggersAfterLifeLoss() {
        harness.addToBattlefield(player1, new StarCharter());
        harness.setHand(player1, List.of());
        Card eligibleCreature = new GrizzlyBears();
        setupTopFour(List.of(new Shock(), new AirElemental(), eligibleCreature, new LlanowarElves()));
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(eligibleCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleCreature);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger when its controller neither gained nor lost life")
    void doesNotTriggerWithoutLifeChange() {
        harness.addToBattlefield(player1, new StarCharter());

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May decline the only eligible card even in a short library")
    void mayDeclineOnlyEligibleCard() {
        harness.addToBattlefield(player1, new StarCharter());
        harness.setHand(player1, List.of());
        Card eligibleCreature = new StarCharter();
        harness.setLibrary(player1, List.of(eligibleCreature));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligibleCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can take a power-three creature and bottoms only the other top four cards")
    void takesPowerThreeCreatureAndPreservesUnseenCards() {
        harness.addToBattlefield(player1, new StarCharter());
        harness.setHand(player1, List.of());
        Card eligibleCreature = new StarCharter();
        Card tooLarge = new AirElemental();
        Card nonCreature = new Shock();
        Card otherEligible = new GrizzlyBears();
        Card unseenFirst = new LlanowarElves();
        Card unseenSecond = new Shock();
        harness.setLibrary(player1, List.of(
                eligibleCreature, tooLarge, nonCreature, otherEligible, unseenFirst, unseenSecond));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).validCardIds())
                .containsExactlyInAnyOrder(eligibleCreature.getId(), otherEligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligibleCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligibleCreature);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck.subList(0, 2)).containsExactly(unseenFirst, unseenSecond);
        assertThat(deck.subList(2, 5)).containsExactlyInAnyOrder(tooLarge, nonCreature, otherEligible);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bottoms all looked-at cards when no creature qualifies")
    void bottomsAllCardsWhenNoneQualify() {
        harness.addToBattlefield(player1, new StarCharter());
        harness.setHand(player1, List.of());
        Card first = new AirElemental();
        Card second = new Shock();
        Card third = new AirElemental();
        Card fourth = new Shock();
        Card unseen = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second, third, fourth, unseen));
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        harness.passBothPriorities();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck.getFirst()).isSameAs(unseen);
        assertThat(deck.subList(1, 5)).containsExactlyInAnyOrder(first, second, third, fourth);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gaining and losing life creates just one trigger even with no net life change")
    void triggersOnceAfterBothLifeGainAndLoss() {
        harness.addToBattlefield(player1, new StarCharter());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("An opponent's life changes do not satisfy the condition")
    void doesNotTriggerForOpponentsLifeChanges() {
        harness.addToBattlefield(player1, new StarCharter());
        gd.lifeGainedThisTurn.put(player2.getId(), 1);
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new StarCharter());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without requiring a choice")
    void resolvesWithEmptyLibrary() {
        harness.addToBattlefield(player1, new StarCharter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupTopFour(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
