package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GongagaReactorTown.class})
class GongagaReactorTownTest extends BaseCardTest {

    @Test
    @DisplayName("Gongaga, Reactor Town enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new GongagaReactorTown()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Gongaga, Reactor Town").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability prompts a choice between red and green")
    void activatingPromptsColorChoice() {
        addCreatureReady(player1, new GongagaReactorTown());
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "GREEN");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"RED", "GREEN"})
    @DisplayName("Choosing a color adds one mana of that color and taps the land")
    void choosingColorAddsThatMana(ManaColor color) {
        Permanent town = addCreatureReady(player1, new GongagaReactorTown());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        for (ManaColor candidate : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(candidate))
                    .isEqualTo(candidate == color ? 1 : 0);
        }
        assertThat(town.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The land cannot produce mana while tapped after entering")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new GongagaReactorTown()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.interaction.activeInteraction()).isNull();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    @DisplayName("An untapped noncreature land can produce mana on the turn it enters")
    void noncreatureLandDoesNotHaveSummoningSicknessRestriction() {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new GongagaReactorTown());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(town.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another activation requires untapping and may choose the other color")
    void canActivateAgainAfterUntapping() {
        Permanent town = addCreatureReady(player1, new GongagaReactorTown());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(town.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
