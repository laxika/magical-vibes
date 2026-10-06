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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("The same card can create a second Beast through flashback after a normal cast")
    void normalCastThenFlashbackCreatesTwoBeasts() {
        ShadowbeastSighting spell = new ShadowbeastSighting();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(beastTokens()).hasSize(1);
        harness.assertInGraveyard(player1, "Shadowbeast Sighting");

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(beastTokens()).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Shadowbeast Sighting");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Flashback cannot be paid with only the normal casting cost")
    void flashbackRequiresItsFullCost() {
        harness.setGraveyard(player1, List.of(new ShadowbeastSighting()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Shadowbeast Sighting");
        assertThat(gd.stack).isEmpty();
        assertThat(beastTokens()).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private List<Permanent> beastTokens() {
        return findPermanents(player1, "Beast").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
