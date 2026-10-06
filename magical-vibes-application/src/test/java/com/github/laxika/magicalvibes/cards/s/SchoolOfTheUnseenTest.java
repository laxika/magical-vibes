package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SchoolOfTheUnseen.class})
class SchoolOfTheUnseenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} immediately (mana ability)")
    void tapForColorlessAddsMana() {
        Permanent school = addReadySchool(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(school.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Second ability prompts for a color choice and adds that mana")
    void secondAbilityAddsChosenColor() {
        Permanent school = addReadySchool(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder(
                ManaColor.WHITE.name(), ManaColor.BLUE.name(), ManaColor.BLACK.name(),
                ManaColor.RED.name(), ManaColor.GREEN.name());
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(school.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Second ability spends {2} from the mana pool")
    void secondAbilitySpendsMana() {
        addReadySchool(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Cannot activate the second ability with only one mana available")
    void cannotActivateSecondAbilityWithoutEnoughMana() {
        Permanent school = addReadySchool(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(school.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent school = addReadySchool(player1);
        school.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Filtering produces exactly one mana of each chosen color")
    void filteringProducesEachColor(ManaColor color) {
        Permanent school = harness.addToBattlefieldAndReturn(player1, new SchoolOfTheUnseen());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
        assertThat(school.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A newly controlled noncreature land can tap for colorless")
    void newlyControlledLandCanProduceColorless() {
        Permanent school = harness.addToBattlefieldAndReturn(player1, new SchoolOfTheUnseen());
        school.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(school.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped land cannot filter mana or spend its activation cost")
    void tappedLandCannotFilterMana() {
        Permanent school = addReadySchool(player1);
        school.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySchool(Player player) {
        return addCreatureReady(player, new SchoolOfTheUnseen());
    }
}
