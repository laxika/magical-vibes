package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SyphonSoulTest extends BaseCardTest {

    private void castSyphonSoul() {
        harness.setHand(player1, List.of(new SyphonSoul()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Syphon Soul deals 2 damage to the other player")
    void dealsDamageToOtherPlayer() {
        harness.setLife(player2, 20);

        castSyphonSoul();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Syphon Soul gains its controller life equal to the damage dealt")
    void gainsLifeEqualToDamage() {
        harness.setLife(player1, 20);

        castSyphonSoul();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Syphon Soul goes to the graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        castSyphonSoul();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Syphon Soul"));
    }
}
