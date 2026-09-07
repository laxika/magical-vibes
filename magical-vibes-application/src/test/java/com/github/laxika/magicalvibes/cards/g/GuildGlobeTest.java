package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GuildGlobe.class)
class GuildGlobeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and draws a card")
    void entersBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new GuildGlobe()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gameData = harness.getGameData();
        int libraryBefore = gameData.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gameData.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    @DisplayName("Pays {2}, sacrifices itself, and adds two mana of different colors")
    void abilityAddsDifferentColorsAndSacrifices() {
        harness.addToBattlefield(player1, new GuildGlobe());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gameData = harness.getGameData();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());
        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.RED.name()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Guild Globe");
        harness.assertInGraveyard(player1, "Guild Globe");
    }
}
