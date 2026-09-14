package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.cards.z.ZuranEnchanter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GerrardsVerdict.class, Forest.class, GrizzlyBears.class,
        TamiyoCollectorOfTales.class, ZuranEnchanter.class})
class GerrardsVerdictTest extends BaseCardTest {

    @Test
    void gainsThreeLifeForEachDiscardedLand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        castVerdict();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player1, 23);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void gainsSixLifeWhenBothDiscardedCardsAreLands() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest())));
        castVerdict();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player1, 26);
    }

    @Test
    void gainsNoLifeWhenNoDiscardedCardIsALand() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        castVerdict();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player1, 20);
    }

    @Test
    void discardsOnlyAvailableCardWhenTargetHasOneCard() {
        harness.setHand(player2, List.of(new Forest()));
        castVerdict();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    void doesNothingWhenTargetHasNoCards() {
        harness.setHand(player2, List.of());
        castVerdict();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void canTargetController() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new GerrardsVerdict(), new Forest(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 23);
    }

    @Test
    void discardPreventionDoesNotLeakLifeGainToLaterDiscard() {
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        addCreatureReady(player2, new ZuranEnchanter());
        var tamiyo = harness.addToBattlefieldAndReturn(player2, new TamiyoCollectorOfTales());
        tamiyo.setCounterCount(CounterType.LOYALTY, 5);
        castVerdict();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);

        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player1, 20);
    }

    private void castVerdict() {
        harness.setHand(player1, List.of(new GerrardsVerdict()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
