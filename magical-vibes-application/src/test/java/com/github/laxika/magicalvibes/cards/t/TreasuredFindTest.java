package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreasuredFindTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target card from your graveyard to your hand and exiles itself")
    void returnsTargetCardAndExilesItself() {
        Card target = new HolyDay();
        Card spell = new TreasuredFind();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new TreasuredFind()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the targeted card leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card target = new HolyDay();
        Card spell = new TreasuredFind();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }
}
