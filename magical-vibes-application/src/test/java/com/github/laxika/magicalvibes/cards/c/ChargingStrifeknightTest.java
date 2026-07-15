package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChargingStrifeknightTest extends BaseCardTest {

    

    @Test
    @DisplayName("Activating ability starts discard-cost choice for any card")
    void activationStartsDiscardChoice() {
        addReadyStrifeknight(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(0, 1);
    }

    @Test
    @DisplayName("Choosing a card pays cost and puts ability on stack")
    void choosingCardPaysCostAndStacksAbility() {
        addReadyStrifeknight(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Charging Strifeknight");
    }

    @Test
    @DisplayName("Cannot activate without cards in hand")
    void cannotActivateWithoutCardsInHand() {
        addReadyStrifeknight(player1);
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving ability draws a card")
    void resolvingDrawsACard() {
        addReadyStrifeknight(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("Can discard any card type as cost — lands work too")
    void canDiscardLandAsCost() {
        addReadyStrifeknight(player1);
        harness.setHand(player1, List.of(new Mountain()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Mountain"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent strifeknight = addReadyStrifeknight(player1);
        strifeknight.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can activate tap ability the turn it enters the battlefield (Haste)")
    void canActivateWithSummoningSicknessDueToHaste() {
        ChargingStrifeknight card = new ChargingStrifeknight();
        Permanent strifeknight = new Permanent(card);
        gd.playerBattlefields.get(player1.getId()).add(strifeknight);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
    }

    @Test
    @DisplayName("Activating ability taps Charging Strifeknight after paying discard cost")
    void activatingTapsStrifeknight() {
        Permanent strifeknight = addReadyStrifeknight(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(strifeknight.isTapped()).isTrue();
    }

    private Permanent addReadyStrifeknight(Player player) {
        ChargingStrifeknight card = new ChargingStrifeknight();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
