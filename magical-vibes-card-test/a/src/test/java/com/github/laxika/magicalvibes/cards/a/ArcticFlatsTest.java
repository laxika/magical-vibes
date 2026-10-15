package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcticFlats.class})
class ArcticFlatsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ArcticFlats()));

        harness.playLand(player1, 0);

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability prompts for green or white")
    void manaAbilityPromptsForGreenOrWhite() {
        addReadyFlats(player1);
        GameData gd = harness.getGameData();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE");
    }

    @Test
    @DisplayName("Choosing green adds one green mana")
    void choosingGreenAddsMana() {
        Permanent flats = addReadyFlats(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(flats.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing white adds one white mana")
    void choosingWhiteAddsMana() {
        Permanent flats = addReadyFlats(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(flats.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while tapped after entering")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new ArcticFlats()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Can produce mana after untapping")
    void canProduceManaAfterUntapping() {
        harness.setHand(player1, List.of(new ArcticFlats()));
        harness.playLand(player1, 0);
        harness.performUntapStep(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "WHITE"})
    @DisplayName("Either mana choice can pay a snow mana cost")
    void producedManaRetainsSnowSource(ManaColor color) {
        harness.addToBattlefield(player1, new ArcticFlats());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowMana(color)).isEqualTo(1);
    }

    private Permanent addReadyFlats(Player player) {
        Permanent flats = harness.addToBattlefieldAndReturn(player, new ArcticFlats());
        flats.setSummoningSick(false);
        return flats;
    }
}
