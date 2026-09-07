package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stormbind.class, BalduvianBears.class, Forest.class})
class StormbindTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 2 damage to a creature and discards a card at random as a cost")
    void damagesCreatureAndDiscardsAtRandom() {
        harness.addToBattlefield(player1, new Stormbind());
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearId = harness.getPermanentId(player2, "Balduvian Bears");
        harness.activateAbility(player1, battlefieldIndex(player1, "Stormbind"), null, bearId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Ability deals 2 damage to a player")
    void damagesPlayer() {
        harness.addToBattlefield(player1, new Stormbind());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Stormbind"), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Ability can be activated more than once without tapping")
    void canBeActivatedMoreThanOnceWithoutTapping() {
        harness.addToBattlefield(player1, new Stormbind());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int stormbindIndex = battlefieldIndex(player1, "Stormbind");
        harness.activateAbility(player1, stormbindIndex, null, player2.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, stormbindIndex, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate with an empty hand (no card to discard)")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new Stormbind());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Stormbind"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player player, String cardName) {
        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(cardName)) {
                return i;
            }
        }
        throw new IllegalStateException("Permanent not found: " + cardName);
    }
}
