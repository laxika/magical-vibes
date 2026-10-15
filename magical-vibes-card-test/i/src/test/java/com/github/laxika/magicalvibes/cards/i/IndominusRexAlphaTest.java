package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SkyknightLegionnaire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndominusRexAlpha.class, SkyknightLegionnaire.class, SerraAngel.class, GrizzlyBears.class,
        RestInPeace.class})
class IndominusRexAlphaTest extends BaseCardTest {

    @Test
    void gainsOneCounterPerKeywordAndDrawsForAllCounters() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(
                new IndominusRexAlpha(), new SkyknightLegionnaire(), new SerraAngel()));
        castRex();

        PendingInteraction.XValueChoice countChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(countChoice.maxValue()).isEqualTo(2);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent rex = findPermanent(player1, "Indominus Rex, Alpha");
        assertThat(rex.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(rex.getCounterCount(CounterType.HASTE)).isEqualTo(1);
        assertThat(rex.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(rex.getTotalCounterCount()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void discardingCreatureWithoutListedKeywordsAddsNoCountersOrCards() {
        harness.setHand(player1, List.of(new IndominusRexAlpha(), new GrizzlyBears()));
        castRex();

        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent rex = findPermanent(player1, "Indominus Rex, Alpha");
        assertThat(rex.getTotalCounterCount()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void discardChoiceHappensBeforeRexIsOnTheBattlefield() {
        harness.setHand(player1, List.of(new IndominusRexAlpha(), new SerraAngel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        assertThat(countPermanents(player1, "Indominus Rex, Alpha")).isZero();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        Permanent rex = findPermanent(player1, "Indominus Rex, Alpha");
        assertThat(rex.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(rex.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
    }

    @Test
    void keywordCountersArePresentBeforeTheDrawTriggerResolves() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new IndominusRexAlpha(), new SerraAngel()));
        castRex();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        Permanent rex = findPermanent(player1, "Indominus Rex, Alpha");
        assertThat(rex.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(rex.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void mayDiscardZeroCreatureCards() {
        SerraAngel angel = new SerraAngel();
        harness.setHand(player1, List.of(new IndominusRexAlpha(), angel));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castRex();
        harness.handleXValueChosen(player1, 0);

        assertThat(findPermanent(player1, "Indominus Rex, Alpha").getTotalCounterCount()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(angel);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void entersWithoutDiscardWhenNoCreatureCardsAreInHand() {
        harness.setHand(player1, List.of(new IndominusRexAlpha()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castRex();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Indominus Rex, Alpha").getTotalCounterCount()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void discardedCreatureStillGrantsCountersWhenRestInPeaceExilesIt() {
        harness.addToBattlefield(player2, new RestInPeace());
        SerraAngel angel = new SerraAngel();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new IndominusRexAlpha(), angel));
        castRex();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(angel.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        Permanent rex = findPermanent(player1, "Indominus Rex, Alpha");
        assertThat(rex.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(rex.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void castRex() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
