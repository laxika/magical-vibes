package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EmergenceZone;
import com.github.laxika.magicalvibes.cards.s.SanitariumSkeleton;
import com.github.laxika.magicalvibes.cards.s.SilverbackShaman;
import com.github.laxika.magicalvibes.cards.s.SparkReaper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BagOfHolding.class, SilverbackShaman.class, SanitariumSkeleton.class,
        EmergenceZone.class, SparkReaper.class})
class BagOfHoldingTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, then discards and tracks the discarded card with the Bag")
    void drawsThenDiscardsAndTracksCard() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfHolding());
        Card discarded = new SilverbackShaman();
        Card drawn = new SilverbackShaman();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(discarded.getId()).sourcePermanentId()).isEqualTo(bag.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(drawn.getId()));
    }

    @Test
    @DisplayName("Sacrifice returns every card exiled with the Bag to its owner's hand")
    void sacrificeReturnsAllExiledCardsToOwnersHands() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfHolding());
        Card firstDiscard = new SilverbackShaman();
        Card secondDiscard = new SilverbackShaman();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(new SilverbackShaman(), new SilverbackShaman()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        bag.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        bag.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Bag of Holding");
        assertThat(gd.playerHands.get(player1.getId()))
                .doesNotContain(firstDiscard, secondDiscard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(firstDiscard.getId()))
                .anyMatch(card -> card.getId().equals(secondDiscard.getId()));
        assertThat(gd.getCardsExiledByPermanent(bag.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Bag of Holding");
    }

    @Test
    void emptyHandDiscardsTheCardJustDrawn() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfHolding());
        Card drawn = new SilverbackShaman();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(drawn.getId()).sourcePermanentId()).isEqualTo(bag.getId());
    }

    @Test
    void doesNotExileDiscardedCardThatReturnedToHandBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new BagOfHolding());
        Card skeleton = new SanitariumSkeleton();
        harness.setHand(player1, List.of(skeleton));
        harness.setLibrary(player1, List.of(new SilverbackShaman()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(skeleton);
        assertThat(gd.findExiledCard(skeleton.getId())).isNull();
    }

    @Test
    void doesNotExileNewGraveyardObjectAfterDiscardedCardLeavesAndReturns() {
        harness.addToBattlefield(player1, new BagOfHolding());
        harness.addToBattlefield(player1, new EmergenceZone());
        Permanent reaper = harness.addToBattlefieldAndReturn(player1, new SparkReaper());
        Card skeleton = new SanitariumSkeleton();
        harness.setHand(player1, List.of(skeleton));
        harness.setLibrary(player1, List.of(new SilverbackShaman(), new SilverbackShaman()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(skeleton));
        harness.passBothPriorities();
        Permanent returnedSkeleton = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(skeleton.getId()))
                .findFirst().orElseThrow();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(reaper),
                null, null);
        harness.handlePermanentChosen(player1, returnedSkeleton.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(skeleton);
        assertThat(gd.findExiledCard(skeleton.getId())).isNull();
    }
}
