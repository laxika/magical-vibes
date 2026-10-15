package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Fugue;
import com.github.laxika.magicalvibes.cards.e.ErraticPortal;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindMaggots.class, RagingGoblin.class, Fugue.class, ErraticPortal.class})
class MindMaggotsTest extends BaseCardTest {

    @Test
    void discardsChosenCreatureCardsAndGetsTwoCountersPerCard() {
        harness.setHand(player1, List.of(new MindMaggots(), new RagingGoblin(), new Fugue(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.XValueChoice countChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(countChoice).isNotNull();
        assertThat(countChoice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 1);

        Permanent maggots = findPermanent(player1, "Mind Maggots");
        assertThat(maggots.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Fugue"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Raging Goblin"))
                .hasSize(2);
    }

    @Test
    void canDiscardOnlySomeEligibleCreatureCards() {
        harness.setHand(player1, List.of(new MindMaggots(), new RagingGoblin(), new Fugue(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 2);

        Permanent maggots = findPermanent(player1, "Mind Maggots");
        assertThat(maggots.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Raging Goblin"))
                .hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Fugue"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Raging Goblin"))
                .hasSize(1);
    }

    @Test
    void choosingZeroDiscardsNoCardsAndAddsNoCounters() {
        harness.setHand(player1, List.of(new MindMaggots(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        Permanent maggots = findPermanent(player1, "Mind Maggots");
        assertThat(maggots.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Raging Goblin"))
                .hasSize(1);
    }

    @Test
    void doesNotOfferNoncreatureCardsForDiscard() {
        harness.setHand(player1, List.of(new MindMaggots(), new Fugue()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Mind Maggots")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void resolvesWithoutAChoiceWhenHandIsEmpty() {
        harness.castFromHand(player1, new MindMaggots(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Mind Maggots")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillDiscardsWhenSourceLeavesBeforeTriggerResolves() {
        Permanent otherMaggots = harness.addToBattlefieldAndReturn(player1, new MindMaggots());
        harness.addToBattlefield(player2, new ErraticPortal());
        harness.castFromHand(player1, new MindMaggots(), "{3}{B}");
        harness.passBothPriorities();

        Permanent enteringMaggots = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(otherMaggots.getId()))
                .findFirst().orElseThrow();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, enteringMaggots.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInHand(player1, "Mind Maggots");

        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Mind Maggots");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(otherMaggots);
        assertThat(otherMaggots.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
