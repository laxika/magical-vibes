package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChimneyGoyf.class, Forest.class, GrizzlyBears.class, LavaSpike.class, Millstone.class,
        Murder.class, Ornithopter.class, Peek.class, Shock.class})
class ChimneyGoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Has 0/1 with empty graveyards")
    void hasBasePowerAndToughnessWithEmptyGraveyards() {
        Permanent goyf = addCreatureReady(player1, new ChimneyGoyf());

        assertThat(gqs.getEffectivePower(gd, goyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power counts distinct card types in all graveyards")
    void countsDistinctCardTypesInAllGraveyards() {
        Permanent goyf = addCreatureReady(player1, new ChimneyGoyf());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone(), new LavaSpike(),
                new Ornithopter()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(6);
    }

    @Test
    @DisplayName("When Chimney Goyf dies, target opponent puts a card from hand on top of their library")
    void deathTriggerPutsOpponentsCardOnTopOfLibrary() {
        Permanent goyf = harness.addToBattlefieldAndReturn(player1, new ChimneyGoyf());
        List<Card> hand = new ArrayList<>(List.of(new GrizzlyBears(), new Peek()));
        harness.setHand(player2, hand);
        Card oldTop = gd.playerDecks.get(player2.getId()).getFirst();

        killGoyf(goyf);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(hand.getFirst().getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(hand.get(1));
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(hand.getFirst(), oldTop);
    }

    private void killGoyf(Permanent goyf) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, new ArrayList<>(List.of(new Murder())));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, goyf.getId());
        harness.passBothPriorities();
    }
}
