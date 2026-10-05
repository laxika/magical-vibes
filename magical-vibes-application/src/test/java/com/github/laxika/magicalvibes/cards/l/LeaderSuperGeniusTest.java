package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AIMScientists;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FrozenInIce;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeaderSuperGenius.class, AIMScientists.class, Forest.class, FrozenInIce.class, Mountain.class})
class LeaderSuperGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, a creature you control connives")
    void beginningOfCombatTargetConnives() {
        harness.addToBattlefield(player1, new LeaderSuperGenius());
        Permanent scientists = harness.addToBattlefieldAndReturn(player1, new AIMScientists());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new AIMScientists()));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, scientists.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("A.I.M. Scientists");

        assertThat(scientists.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Mountain");
    }

    @Test
    @DisplayName("Adds a draw before a creature's connive")
    void replacementAddsDrawBeforeConnive() {
        harness.addToBattlefield(player1, new LeaderSuperGenius());
        harness.setHand(player1, List.of(new AIMScientists(), new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new AIMScientists()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent scientists = findPermanent(player1, "A.I.M. Scientists");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("A.I.M. Scientists");

        assertThat(scientists.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Mountain");
    }

    @Test
    void discardingALandDoesNotAddACounter() {
        Permanent leader = harness.addToBattlefieldAndReturn(player1, new LeaderSuperGenius());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new AIMScientists()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, leader.getId());
        harness.passBothPriorities();
        discardByName("Mountain");

        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "A.I.M. Scientists");
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new LeaderSuperGenius());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new AIMScientists()));

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void doesNotAddADrawForOpponentsConnive() {
        harness.addToBattlefield(player1, new LeaderSuperGenius());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AIMScientists(), new Forest()));
        harness.setLibrary(player2, List.of(new Mountain(), new AIMScientists()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player2, "A.I.M. Scientists")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void combatTriggerStillResolvesAfterLeaderLeavesWithoutTheExtraDraw() {
        Permanent leader = harness.addToBattlefieldAndReturn(player1, new LeaderSuperGenius());
        Permanent scientists = harness.addToBattlefieldAndReturn(player1, new AIMScientists());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new AIMScientists(), new Mountain()));

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, scientists.getId());
        gd.playerBattlefields.get(player1.getId()).remove(leader);
        harness.passBothPriorities();
        discardByName("A.I.M. Scientists");

        assertThat(scientists.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void losingAbilitiesDisablesTheReplacementDraw() {
        Permanent leader = harness.addToBattlefieldAndReturn(player1, new LeaderSuperGenius());
        harness.setHand(player1, List.of(new FrozenInIce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, leader.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, leader)).isTrue();

        harness.setHand(player1, List.of(new AIMScientists(), new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new AIMScientists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Forest");

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "A.I.M. Scientists")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
