package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ArchaeologicalDig.class)
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

    @Test
    @DisplayName("Cannot activate the sacrifice ability after Archaeological Dig is tapped")
    void cannotActivateSacrificeAbilityAfterTapping() {
        Permanent dig = harness.addToBattlefieldAndReturn(player1, new ArchaeologicalDig());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(dig.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Archaeological Dig");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Sacrifice ability immediately adds exactly one mana of any chosen color")
    void sacrificeProducesEachColorWithoutUsingStack(ManaColor color) {
        harness.addToBattlefield(player1, new ArchaeologicalDig());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.assertNotOnBattlefield(player1, "Archaeological Dig");
        harness.assertInGraveyard(player1, "Archaeological Dig");

        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless ability resolves immediately and cannot be activated twice while tapped")
    void colorlessAbilityRequiresUntappedLandAndDoesNotUseStack() {
        Permanent dig = harness.addToBattlefieldAndReturn(player1, new ArchaeologicalDig());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(dig.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Archaeological Dig");
    }
}
