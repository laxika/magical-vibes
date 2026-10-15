package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.d.DrannithStinger;
import com.github.laxika.magicalvibes.cards.n.Neutralize;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RielleTheEverwise.class, FaithlessLooting.class, GrizzlyBears.class, DrannithStinger.class, Neutralize.class})
class RielleTheEverwiseTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each instant and sorcery card in your graveyard")
    void powerCountsOwnInstantsAndSorceriesInGraveyard() {
        Permanent rielle = addRielleReady(player1);
        harness.setGraveyard(player1, List.of(
                new FaithlessLooting(), new FaithlessLooting(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new FaithlessLooting()));

        assertThat(gqs.getEffectivePower(gd, rielle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rielle)).isEqualTo(3);
    }

    @Test
    @DisplayName("Draws the number of cards discarded in the first discard event each turn")
    void drawsForOnlyTheFirstDiscardEventEachTurn() {
        FaithlessLooting firstLooting = new FaithlessLooting();
        FaithlessLooting secondLooting = new FaithlessLooting();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(
                new RielleTheEverwise(), firstLooting, secondLooting,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        int secondLootingIndex = findCardIndex(player1, secondLooting);
        harness.castSorcery(player1, secondLootingIndex, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    void doesNotTriggerIfControllerDiscardedBeforeRielleEntered() {
        harness.setHand(player1, List.of(new DrannithStinger(), new DrannithStinger()));
        harness.setLibrary(player1, List.of(new DrannithStinger(), new DrannithStinger(),
                new DrannithStinger(), new DrannithStinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        addRielleReady(player1);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void cyclingTriggersDrawBeforeCyclingResolvesAndOnlyOncePerTurn() {
        addRielleReady(player1);
        harness.setHand(player1, List.of(new DrannithStinger(), new DrannithStinger()));
        harness.setLibrary(player1, List.of(new DrannithStinger(), new DrannithStinger(),
                new DrannithStinger(), new DrannithStinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void opponentsDiscardDoesNotTriggerAndControllerCanTriggerOnOpponentsTurn() {
        addRielleReady(player1);
        harness.setHand(player1, List.of(new DrannithStinger()));
        harness.setHand(player2, List.of(new DrannithStinger()));
        harness.setLibrary(player1, List.of(new DrannithStinger(), new DrannithStinger()));
        harness.setLibrary(player2, List.of(new DrannithStinger(), new DrannithStinger()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player2, 0, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void powerUpdatesWhenInstantAndSorceryCardsLeaveGraveyard() {
        Permanent rielle = addRielleReady(player1);
        harness.setGraveyard(player1, List.of(new Neutralize(), new FaithlessLooting(),
                new DrannithStinger()));
        assertThat(gqs.getEffectivePower(gd, rielle)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new DrannithStinger()));
        assertThat(gqs.getEffectivePower(gd, rielle)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, rielle)).isEqualTo(3);
    }
    @Test
    void firstDiscardDrawResetsOnTheNextTurn() {
        addRielleReady(player1);
        harness.setHand(player1, List.of(new DrannithStinger(), new DrannithStinger()));
        harness.setLibrary(player1, List.of(new DrannithStinger(), new DrannithStinger(),
                new DrannithStinger(), new DrannithStinger()));
        harness.setLibrary(player2, List.of(new DrannithStinger(), new DrannithStinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }
    private Permanent addRielleReady(Player player) {
        return addCreatureReady(player, new RielleTheEverwise());
    }

    private int findCardIndex(Player player, Card card) {
        List<Card> hand = gd.playerHands.get(player.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getId().equals(card.getId())) {
                return i;
            }
        }
        throw new AssertionError("Card is not in hand");
    }
}
