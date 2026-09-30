package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GodlessShrine;
import com.github.laxika.magicalvibes.cards.m.MourningThrull;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CausticRain.class, GodlessShrine.class, MourningThrull.class})
class CausticRainTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Caustic Rain exiles target land")
    void exilesTargetLand() {
        harness.addToBattlefield(player2, new GodlessShrine());
        harness.setHand(player1, List.of(new CausticRain()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Godless Shrine"));
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Godless Shrine"));
        harness.assertNotOnBattlefield(player2, "Godless Shrine");
        harness.assertNotInGraveyard(player2, "Godless Shrine");
    }

    @Test
    @DisplayName("Can exile your own land")
    void exilesOwnLand() {
        harness.addToBattlefield(player1, new GodlessShrine());
        harness.setHand(player1, List.of(new CausticRain()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Godless Shrine"));
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Godless Shrine"));
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new MourningThrull());
        harness.setHand(player1, List.of(new CausticRain()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Mourning Thrull")))
                .isInstanceOf(IllegalStateException.class);
    }
}
