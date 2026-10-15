package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SacredPeaks.class)
class SacredPeaksTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SacredPeaks()));

        harness.playLand(player1, 0);

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability prompts for red or white")
    void manaAbilityPromptsForRedOrWhite() {
        harness.addToBattlefieldAndReturn(player1, new SacredPeaks());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gameData = harness.getGameData();
        PendingInteraction.ColorChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "WHITE");
    }

    @Test
    void choosingRedAddsOneRedMana() {
        Permanent peaks = harness.addToBattlefieldAndReturn(player1, new SacredPeaks());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(peaks.isTapped()).isTrue();
    }

    @Test
    void choosingWhiteAddsOneWhiteMana() {
        Permanent peaks = harness.addToBattlefieldAndReturn(player1, new SacredPeaks());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(peaks.isTapped()).isTrue();
    }

    @Test
    void entersTappedWhenPutOntoBattlefieldWithoutBeingPlayed() {
        Permanent peaks = harness.enterBattlefieldAndReturn(player1, new SacredPeaks());

        assertThat(peaks.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotProduceManaWhileTapped() {
        harness.enterBattlefieldAndReturn(player1, new SacredPeaks());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void newlyEnteredLandProducesOnlyChosenManaAfterUntappingWithoutUsingStack() {
        Permanent peaks = harness.enterBattlefieldAndReturn(player1, new SacredPeaks());
        peaks.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "WHITE");

        assertThat(peaks.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
