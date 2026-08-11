package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoarOfTheWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Roar of the Wurm creates a 6/6 green Wurm token")
    void createsWurmToken() {
        harness.setHand(player1, List.of(new RoarOfTheWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent wurm = wurmTokens().getFirst();
        assertThat(wurm.getCard().getPower()).isEqualTo(6);
        assertThat(wurm.getCard().getToughness()).isEqualTo(6);
        assertThat(wurm.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wurm.getCard().getSubtypes()).contains(CardSubtype.WURM);
        harness.assertInGraveyard(player1, "Roar of the Wurm");
    }

    @Test
    @DisplayName("Flashback creates a Wurm token and exiles Roar of the Wurm")
    void flashbackCreatesWurmAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new RoarOfTheWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(wurmTokens()).hasSize(1);
        harness.assertNotInGraveyard(player1, "Roar of the Wurm");
        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Roar of the Wurm"));
    }

    private List<Permanent> wurmTokens() {
        return harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Wurm"))
                .toList();
    }
}
