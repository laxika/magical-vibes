package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.MyriadConstruct;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScrapyardRecombiner.class, BronzeSable.class, Assassinate.class, GrizzlyBears.class,
        MindStone.class, MyriadConstruct.class})
class ScrapyardRecombinerTest extends BaseCardTest {

    @Test
    void entersWithTwoPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new ScrapyardRecombiner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent recombiner = findPermanent(player1, "Scrapyard Recombiner");
        assertThat(recombiner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent recombiner = addCreatureReady(player1, new ScrapyardRecombiner());
        recombiner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        recombiner.tap();
        Permanent bronzeSable = addCreatureReady(player1, new BronzeSable());

        destroyRecombiner(recombiner);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bronzeSable.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bronzeSable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void sacrificesAnArtifactAndSearchesForAConstruct() {
        Permanent recombiner = addCreatureReady(player1, new ScrapyardRecombiner());
        harness.addToBattlefield(player1, new MindStone());
        Permanent mindStone = findPermanent(player1, "Mind Stone");
        setLibrary(new MyriadConstruct(), new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, mindStone.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Myriad Construct");

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Myriad Construct"));
        assertThat(recombiner.isTapped()).isTrue();
    }

    private void destroyRecombiner(Permanent recombiner) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, recombiner.getId(), null);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
