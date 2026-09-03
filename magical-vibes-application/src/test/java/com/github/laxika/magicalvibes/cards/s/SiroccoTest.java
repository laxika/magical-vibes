package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.m.MemoryLapse;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sirocco.class, MemoryLapse.class, StalkingTiger.class, Incinerate.class,
        TamiyoCollectorOfTales.class, SpiritualFocus.class})
class SiroccoTest extends BaseCardTest {

    private void castSiroccoOn(int targetLife, List<Card> targetHand) {
        castSiroccoOn(player2, targetLife, targetHand);
    }

    private void castSiroccoOn(Player targetPlayer, int targetLife, List<Card> targetHand) {
        harness.setLife(targetPlayer, targetLife);
        if (targetPlayer.equals(player1)) {
            List<Card> casterHand = new ArrayList<>();
            casterHand.add(new Sirocco());
            casterHand.addAll(targetHand);
            harness.setHand(player1, casterHand);
        } else {
            harness.setHand(player2, new ArrayList<>(targetHand));
            harness.setHand(player1, List.of(new Sirocco()));
        }

        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, targetPlayer.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying 4 life keeps the revealed blue instant")
    void paysLifeKeepsCard() {
        castSiroccoOn(20, List.of(new MemoryLapse(), new StalkingTiger()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining discards that blue instant; non-matching cards stay in hand")
    void declineDiscardsOnlyMatchingCard() {
        castSiroccoOn(20, List.of(new MemoryLapse(), new StalkingTiger(), new Incinerate()));

        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Memory Lapse");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each blue instant is a separate decision")
    void oneDecisionPerBlueInstant() {
        castSiroccoOn(20, List.of(new MemoryLapse(), new MemoryLapse()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        // Second prompt for the other blue instant.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A player who can't pay 4 life discards with no prompt")
    void cannotPayDiscardsAutomatically() {
        castSiroccoOn(3, List.of(new MemoryLapse(), new StalkingTiger()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 3);
        harness.assertInGraveyard(player2, "Memory Lapse");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No blue instants revealed — nothing is discarded")
    void noMatchingCardsNoEffect() {
        castSiroccoOn(20, List.of(new StalkingTiger(), new Incinerate()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting yourself processes your own hand")
    void canTargetYourself() {
        castSiroccoOn(player1, 20, List.of(new MemoryLapse()));

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Memory Lapse");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Targeting yourself does not trigger opponent-discard abilities")
    void selfTargetDiscardIsNotOpponentCaused() {
        harness.addToBattlefield(player1, new SpiritualFocus());
        castSiroccoOn(player1, 20, List.of(new MemoryLapse()));

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Tamiyo prevents Sirocco from discarding an opponent's card")
    void opponentDiscardPreventionApplies() {
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player2, new TamiyoCollectorOfTales());
        tamiyo.setCounterCount(CounterType.LOYALTY, 5);
        castSiroccoOn(20, List.of(new MemoryLapse()));

        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Memory Lapse");
        harness.assertNotInGraveyard(player2, "Memory Lapse");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
