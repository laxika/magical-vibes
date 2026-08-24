package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(ShadowbeastSighting.class)
class ShadowbeastSightingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Shadowbeast Sighting creates a 4/4 green Beast token")
    void createsBeastToken() {
        harness.setHand(player1, List.of(new ShadowbeastSighting()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> beasts = beastTokens();
        assertThat(beasts).hasSize(1);
        assertThat(beasts.getFirst().getCard().getPower()).isEqualTo(4);
        assertThat(beasts.getFirst().getCard().getToughness()).isEqualTo(4);
        assertThat(beasts.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beasts.getFirst().getCard().getSubtypes()).contains(CardSubtype.BEAST);
        harness.assertInGraveyard(player1, "Shadowbeast Sighting");
    }

    @Test
    @DisplayName("Flashback creates a Beast token and exiles Shadowbeast Sighting")
    void flashbackCreatesBeastAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new ShadowbeastSighting()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(beastTokens()).hasSize(1);
        harness.assertNotInGraveyard(player1, "Shadowbeast Sighting");
        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shadowbeast Sighting"));
    }

    private List<Permanent> beastTokens() {
        return harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Beast"))
                .toList();
    }
}
