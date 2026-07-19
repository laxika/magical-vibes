package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterTransmuterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the chosen artifact and puts an artifact from hand onto the battlefield")
    void returnsArtifactAndPutsArtifactFromHand() {
        harness.addToBattlefield(player1, new MasterTransmuter());
        harness.addToBattlefield(player1, new GoldMyr());
        readyTransmuter();
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID goldMyrId = findPermanent(player1, "Gold Myr").getId();

        harness.activateAbility(player1, 0, null, null);
        // Two artifacts you control (Transmuter + Gold Myr) -> choose which to return.
        harness.handlePermanentChosen(player1, goldMyrId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        // Put the returned Gold Myr back onto the battlefield.
        harness.handleCardChosen(player1, handIndexOf(player1, "Gold Myr"));

        Permanent goldMyr = findPermanent(player1, "Gold Myr");
        assertThat(goldMyr).isNotNull();
        assertThat(goldMyr.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Gold Myr"));
    }

    @Test
    @DisplayName("Can return itself to hand to pay the cost")
    void canReturnItselfToPayTheCost() {
        harness.addToBattlefield(player1, new MasterTransmuter());
        readyTransmuter();
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Transmuter is the only artifact you control, so it is returned automatically.
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Master Transmuter"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Master Transmuter"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Only artifact cards in hand may be put onto the battlefield")
    void onlyArtifactCardsAreOffered() {
        harness.addToBattlefield(player1, new MasterTransmuter());
        harness.addToBattlefield(player1, new GoldMyr());
        readyTransmuter();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Gold Myr").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        List<Integer> validIndices = ((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices();
        // The returned Gold Myr is a valid choice; the Mountain (a land) is not.
        assertThat(validIndices).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).get(validIndices.getFirst()).getName())
                .isEqualTo("Gold Myr");
    }

    @Test
    @DisplayName("Declining the may leaves the returned artifact in hand")
    void decliningLeavesArtifactInHand() {
        harness.addToBattlefield(player1, new MasterTransmuter());
        harness.addToBattlefield(player1, new GoldMyr());
        readyTransmuter();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Gold Myr").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gold Myr"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Gold Myr"));
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new MasterTransmuter());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyTransmuter() {
        findPermanent(player1, "Master Transmuter").setSummoningSick(false);
    }

    private int handIndexOf(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        List<com.github.laxika.magicalvibes.model.Card> hand = gd.playerHands.get(player.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                return i;
            }
        }
        throw new IllegalStateException(cardName + " not in hand");
    }
}
