package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({MeteorStorm.class, Forest.class, Mountain.class, KavuAggressor.class})
class MeteorStormTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 4 damage to a creature and discards two cards at random as costs")
    void damagesCreatureAndDiscardsTwoCardsAtRandom() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.addToBattlefield(player2, new KavuAggressor());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        addActivationMana();

        UUID kavuId = harness.getPermanentId(player2, "Kavu Aggressor");
        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, kavuId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Kavu Aggressor");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Mountain");
    }

    @Test
    @DisplayName("Ability discards exactly two cards when more than two are in hand")
    void discardsExactlyTwoCards() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain(), new Forest()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 16);
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
    @DisplayName("Ability can target its controller")
    void damagesController() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player1, 20);
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(player1, "Meteor Storm"), null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
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

    @Test
    @DisplayName("Cannot activate without choosing an any target")
    void cannotActivateWithoutTarget() {
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Meteor Storm"), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Mountain");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotalAllMana())
                .isEqualTo(4);
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
