package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcornHarvest.class})
class AcornHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Acorn Harvest creates two 1/1 green Squirrel tokens")
    void createsTwoSquirrelTokens() {
        harness.setHand(player1, List.of(new AcornHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> squirrels = squirrelTokens();
        assertThat(squirrels).hasSize(2);
        assertThat(squirrels).allSatisfy(squirrel -> {
            assertThat(squirrel.getCard().getPower()).isEqualTo(1);
            assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
            assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(squirrel.getCard().getSubtypes()).contains(CardSubtype.SQUIRREL);
            assertThat(squirrel.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("Flashback creates Squirrels, pays 3 life, and exiles Acorn Harvest")
    void flashbackCreatesSquirrelsPaysLifeAndExiles() {
        harness.setGraveyard(player1, List.of(new AcornHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(squirrelTokens()).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        harness.assertNotInGraveyard(player1, "Acorn Harvest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Acorn Harvest"));
    }

    private List<Permanent> squirrelTokens() {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Squirrel"))
                .toList();
    }
}
