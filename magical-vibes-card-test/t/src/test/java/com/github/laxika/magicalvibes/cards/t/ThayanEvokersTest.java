package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThayanEvokers.class, LightningBolt.class})
class ThayanEvokersTest extends BaseCardTest {

    @Test
    void entersConjuresLightningBoltThenDiscardsAndGrows() {
        ThayanEvokers discarded = new ThayanEvokers();
        harness.setHand(player1, List.of(discarded));

        Permanent evokers = harness.enterBattlefieldAndReturn(player1, new ThayanEvokers());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discarded));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(LightningBolt.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(evokers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doubleTeamConjureAlsoPutsACounterOnThayanEvokers() {
        Permanent evokers = addCreatureReady(player1, new ThayanEvokers());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(evokers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Card copy = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card instanceof ThayanEvokers)
                .findFirst()
                .orElseThrow();
        assertThat(copy.getKeywords()).doesNotContain(com.github.laxika.magicalvibes.model.Keyword.DOUBLE_TEAM);
    }

    @Test
    void emptyHandStillDiscardsTheConjuredBoltAndGrows() {
        harness.setHand(player1, List.of());

        Permanent evokers = harness.enterBattlefieldAndReturn(player1, new ThayanEvokers());
        resolveAllTriggers();
        assertThat(evokers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().isInstanceOf(LightningBolt.class);
        assertThat(evokers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canDiscardTheConjuredBoltInsteadOfAnExistingCard() {
        ThayanEvokers retained = new ThayanEvokers();
        harness.setHand(player1, List.of(retained));

        Permanent evokers = harness.enterBattlefieldAndReturn(player1, new ThayanEvokers());
        resolveAllTriggers();

        int boltIndex = gd.playerHands.get(player1.getId()).stream()
                .filter(LightningBolt.class::isInstance)
                .mapToInt(card -> gd.playerHands.get(player1.getId()).indexOf(card))
                .findFirst().orElseThrow();
        harness.handleCardChosen(player1, boltIndex);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().isInstanceOf(LightningBolt.class);
        assertThat(evokers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachControlledEvokerGrowsButOpponentsEvokerDoesNot() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ThayanEvokers());
        harness.setHand(player1, List.of());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new ThayanEvokers());
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
