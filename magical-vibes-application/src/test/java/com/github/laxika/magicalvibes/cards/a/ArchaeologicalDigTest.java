package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArchaeologicalDigTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapAddsColorlessMana() {
        harness.addToBattlefield(player1, new ArchaeologicalDig());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Archaeological Dig");
    }

    @Test
    @DisplayName("Sacrificing prompts for a mana color")
    void sacrificeAbilityPromptsForManaColor() {
        harness.addToBattlefield(player1, new ArchaeologicalDig());

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.assertNotOnBattlefield(player1, "Archaeological Dig");
        harness.assertInGraveyard(player1, "Archaeological Dig");
    }

    @Test
    @DisplayName("Choosing a color after sacrificing adds one mana of that color")
    void sacrificeAbilityAddsChosenColorMana() {
        harness.addToBattlefield(player1, new ArchaeologicalDig());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Archaeological Dig");
        harness.assertInGraveyard(player1, "Archaeological Dig");
    }
}
