package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CoffinPurge.class)
class CoffinPurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target card from an opponent's graveyard")
    void exilesTargetCardFromOpponentsGraveyard() {
        Card target = new CoffinPurge();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new CoffinPurge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Exiles a target card from its controller's graveyard")
    void exilesTargetCardFromControllersGraveyard() {
        Card target = new CoffinPurge();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new CoffinPurge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesSpellAfterResolving() {
        Card target = new CoffinPurge();
        harness.setGraveyard(player2, List.of(target));
        Card purge = new CoffinPurge();
        harness.setGraveyard(player1, List.of(purge));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(purge.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(purge.getId()));
    }
}
