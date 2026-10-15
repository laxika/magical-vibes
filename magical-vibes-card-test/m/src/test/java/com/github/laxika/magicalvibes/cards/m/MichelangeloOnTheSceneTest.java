package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MichelangeloOnTheScene.class, Forest.class, WrathOfGod.class})
class MichelangeloOnTheSceneTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each land its controller controls")
    void entersWithCountersForControlledLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new MichelangeloOnTheScene(), "{4}{G}{G}");
        harness.passBothPriorities();

        Permanent michelangelo = findPermanent(player1, "Michelangelo, On the Scene");
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enters without counters when its controller has no lands")
    void entersWithoutCountersWithNoControlledLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new MichelangeloOnTheScene(), "{4}{G}{G}");
        harness.passBothPriorities();

        Permanent michelangelo = findPermanent(player1, "Michelangelo, On the Scene");
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts lands when entering rather than when cast")
    void countsLandsAtResolution() {
        harness.castFromHand(player1, new MichelangeloOnTheScene(), "{4}{G}{G}");
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        Permanent michelangelo = findPermanent(player1, "Michelangelo, On the Scene");
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.addToBattlefield(player1, new Forest());
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature controlled by an opponent returns to its owner's hand")
    void returnsToOwnerRatherThanController() {
        MichelangeloOnTheScene card = new MichelangeloOnTheScene();
        card.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, card);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Returns to its owner's hand when it dies")
    void returnsToOwnersHandWhenItDies() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloOnTheScene());
        Card michelangeloCard = michelangelo.getCard();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(michelangeloCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(michelangeloCard.getId()));
    }

    @Test
    @DisplayName("Does not return a card removed from the graveyard before its death trigger resolves")
    void doesNotReturnCardThatLeftGraveyard() {
        Permanent michelangelo = harness.addToBattlefieldAndReturn(player1, new MichelangeloOnTheScene());
        Card card = michelangelo.getCard();
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.stack).isNotEmpty();

        gd.playerGraveyards.get(player1.getId()).remove(card);
        harness.setExile(player1, List.of(card));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }
}
