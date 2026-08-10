package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OgreShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 2 damage to a creature and discards a card at random as a cost")
    void damagesCreatureAndDiscardsAtRandom() {
        addOgreShaman();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, battlefieldIndex(player1, "Ogre Shaman"), null, bearId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Ability deals 2 damage to a player")
    void damagesPlayer() {
        addOgreShaman();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Ogre Shaman"), null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithEmptyHand() {
        addOgreShaman();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Ogre Shaman"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addOgreShaman() {
        harness.addToBattlefield(player1, new OgreShaman());
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
