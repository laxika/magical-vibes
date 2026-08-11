package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MomentsPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Moment's Peace prevents all combat damage this turn")
    void preventsAllCombatDamage() {
        harness.setHand(player1, List.of(new MomentsPeace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Flashback prevents combat damage and exiles Moment's Peace")
    void flashbackPreventsCombatDamageAndExiles() {
        MomentsPeace card = new MomentsPeace();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiledCard -> exiledCard.getId().equals(card.getId()));
    }
}
