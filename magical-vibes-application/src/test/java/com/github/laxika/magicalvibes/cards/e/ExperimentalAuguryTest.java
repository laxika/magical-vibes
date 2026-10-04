package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.a.AxiomEngraver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExperimentalAugury.class, CopperLonglegs.class, AxiomEngraver.class})
class ExperimentalAuguryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top three cards into hand and the rest on the bottom")
    void choosesOneOfTopThreeAndOrdersRest() {
        Card top1 = new CopperLonglegs();
        Card top2 = new AxiomEngraver();
        Card top3 = new CopperLonglegs();
        harness.setLibrary(player1, List.of(top1, top2, top3));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.setHand(player1, List.of(new ExperimentalAugury()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(top2).doesNotContain(top1, top3);
        assertThat(deck).containsExactly(top3, top1);
    }

    @Test
    @DisplayName("Proliferates after choosing and ordering the top three cards")
    void proliferatesAfterLibrarySelection() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Card top1 = new CopperLonglegs();
        Card top2 = new AxiomEngraver();
        Card top3 = new CopperLonglegs();
        harness.setLibrary(player1, List.of(top1, top2, top3));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.setHand(player1, List.of(new ExperimentalAugury()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top1);
        assertThat(deck).containsExactly(top2, top3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsRemainderBelowCardsOutsideTheTopThree() {
        Card first = new CopperLonglegs();
        Card second = new AxiomEngraver();
        Card third = new CopperLonglegs();
        Card fourth = new AxiomEngraver();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castAugury();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void choosesFromTwoCardsAndStillProliferates() {
        Card first = new CopperLonglegs();
        Card second = new AxiomEngraver();
        harness.setLibrary(player1, List.of(first, second));
        gd.playerPoisonCounters.put(player2.getId(), 1);

        castAugury();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsOnlyLibraryCardIntoHandAndAllowsChoosingNothingToProliferate() {
        Card only = new CopperLonglegs();
        harness.setLibrary(player1, List.of(only));
        gd.playerPoisonCounters.put(player2.getId(), 1);

        castAugury();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryStillProliferatesEveryCounterKindOnChosenPermanentsAndPlayers() {
        harness.setLibrary(player1, List.of());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new AxiomEngraver());
        chosen.setCounterCount(CounterType.OIL, 2);
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent withoutCounters = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);

        castAugury();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId(), player2.getId()));

        assertThat(chosen.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(withoutCounters.getCounters()).isEmpty();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAugury() {
        harness.setHand(player1, List.of(new ExperimentalAugury()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
