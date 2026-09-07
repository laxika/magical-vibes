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

    private void setupTopFour(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
