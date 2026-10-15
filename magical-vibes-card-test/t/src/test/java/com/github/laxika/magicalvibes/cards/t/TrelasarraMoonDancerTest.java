package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrelasarraMoonDancer.class, HillGiantHerdgorger.class})
class TrelasarraMoonDancerTest extends BaseCardTest {

    @Test
    @DisplayName("Gaining life puts a counter on Trelasarra and scries 1")
    void gainingLifePutsCounterAndScries() {
        Permanent trelasarra = harness.addToBattlefieldAndReturn(player1, new TrelasarraMoonDancer());
        HillGiantHerdgorger topCard = new HillGiantHerdgorger();
        harness.setLibrary(player1, List.of(topCard));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        assertThat(trelasarra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An opponent gaining life does not trigger Trelasarra")
    void opponentGainingLifeDoesNotTrigger() {
        Permanent trelasarra = harness.addToBattlefieldAndReturn(player1, new TrelasarraMoonDancer());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        harness.passBothPriorities();

        assertThat(trelasarra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Separate life gains each put one counter and scry independently")
    void separateLifeGainsTriggerSeparately() {
        Permanent trelasarra = harness.addToBattlefieldAndReturn(player1, new TrelasarraMoonDancer());
        HillGiantHerdgorger firstCard = new HillGiantHerdgorger();
        TrelasarraMoonDancer secondCard = new TrelasarraMoonDancer();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5);
        });
        resolveAllTriggers();

        assertThat(trelasarra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(firstCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        resolveAllTriggers();

        assertThat(trelasarra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, firstCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The counter is still placed when the library is empty")
    void emptyLibraryDoesNotPreventCounter() {
        Permanent trelasarra = harness.addToBattlefieldAndReturn(player1, new TrelasarraMoonDancer());
        harness.setLibrary(player1, List.of());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        resolveAllTriggers();

        assertThat(trelasarra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gaining zero life does not trigger Trelasarra")
    void zeroLifeGainDoesNotTrigger() {
        Permanent trelasarra = harness.addToBattlefieldAndReturn(player1, new TrelasarraMoonDancer());
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));
        resolveAllTriggers();

        assertThat(trelasarra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability still scries after Trelasarra leaves the battlefield")
    void sourceLeavingDoesNotPreventScry() {
        Permanent trelasarra = harness.addToBattlefieldAndReturn(player1, new TrelasarraMoonDancer());
        HillGiantHerdgorger topCard = new HillGiantHerdgorger();
        TrelasarraMoonDancer nextCard = new TrelasarraMoonDancer();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.inMutationScope(() -> {
            gd.playerBattlefields.get(player1.getId()).remove(trelasarra);
            gd.playerGraveyards.get(player1.getId()).add(trelasarra.getCard());
        });
        resolveAllTriggers();

        assertThat(trelasarra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
