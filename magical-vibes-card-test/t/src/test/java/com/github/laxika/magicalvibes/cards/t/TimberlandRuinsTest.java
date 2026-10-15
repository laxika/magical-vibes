package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TimberlandRuins.class)
class TimberlandRuinsTest extends BaseCardTest {
    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Sacrificing produces exactly one mana of any of the five colors without using the stack")
    void sacrificeProducesEachColorImmediately(ManaColor color) {
        harness.addToBattlefield(player1, new TimberlandRuins());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        harness.assertNotOnBattlefield(player1, "Timberland Ruins");
        harness.assertInGraveyard(player1, "Timberland Ruins");
    }

    @Test
    @DisplayName("Neither mana ability can be activated while tapped from entering the battlefield")
    void cannotActivateOnEnteringTapped() {
        harness.setHand(player1, List.of(new TimberlandRuins()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertOnBattlefield(player1, "Timberland Ruins");
        harness.assertNotInGraveyard(player1, "Timberland Ruins");
    }

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new TimberlandRuins()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Timberland Ruins").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one green mana")
    void tapAddsGreenMana() {
        harness.addToBattlefield(player1, new TimberlandRuins());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Timberland Ruins");
    }

    @Test
    @DisplayName("Tap and sacrifice adds one mana of the chosen color and moves the land to the graveyard")
    void sacrificeAddsChosenColorMana() {
        harness.addToBattlefield(player1, new TimberlandRuins());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Timberland Ruins");
        harness.assertInGraveyard(player1, "Timberland Ruins");
    }

    @Test
    @DisplayName("Both mana abilities require an untapped Timberland Ruins")
    void manaAbilitiesRequireUntappedSource() {
        harness.addToBattlefield(player1, new TimberlandRuins());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Timberland Ruins");
    }

    @Test
    @DisplayName("The sacrifice ability pays its cost before the mana color is chosen")
    void sacrificeCostIsPaidBeforeColorChoice() {
        harness.addToBattlefield(player1, new TimberlandRuins());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertNotOnBattlefield(player1, "Timberland Ruins");
        harness.assertInGraveyard(player1, "Timberland Ruins");

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
