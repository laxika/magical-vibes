package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

class MeteorStormTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 4 damage to a creature and discards two cards at random as costs")
    void damagesCreatureAndDiscardsTwoCardsAtRandom() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        addActivationMana();

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, bearId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Mountain");
    }

    @Test
    @DisplayName("Ability deals 4 damage to a player")
    void damagesPlayer() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Cannot activate with fewer than two cards in hand")
    void cannotActivateWithFewerThanTwoCards() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
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
