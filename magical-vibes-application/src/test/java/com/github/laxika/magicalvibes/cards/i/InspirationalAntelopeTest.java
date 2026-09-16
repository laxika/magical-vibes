package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LotusCobra;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspirationalAntelope.class, LotusCobra.class, SerraAngel.class})
class InspirationalAntelopeTest extends BaseCardTest {

    @Test
    void openingHandChoiceStoresTheChosenKeyword() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        InspirationalAntelope antelope = new InspirationalAntelope();
        openingHarness.setHand(openingPlayer, List.of(antelope));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        openingHarness.handleMayAbilityChosen(openingPlayer, true);
        assertThat(openingHarness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ColorChoice.class);

        openingHarness.handleListChoice(openingPlayer, "FLYING");

        assertThat(openingHarness.getGameData().legacyChosenWordsByCardId)
                .containsEntry(antelope.getId(), "FLYING");
        assertThat(openingHarness.getGameData().status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void chosenKeywordReducesMatchingSpells() {
        putAntelopeOnBattlefieldAfterChoosing("FLYING");

        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void chosenAbilityWordReducesMatchingSpells() {
        putAntelopeOnBattlefieldAfterChoosing("LANDFALL");

        harness.setHand(player1, List.of(new LotusCobra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void spellsWithoutTheChosenWordAreNotReduced() {
        putAntelopeOnBattlefieldAfterChoosing("FLYING");

        harness.setHand(player1, List.of(new LotusCobra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void putAntelopeOnBattlefieldAfterChoosing(String chosenWord) {
        InspirationalAntelope antelope = new InspirationalAntelope();
        harness.setHand(player1, List.of(antelope));
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, chosenWord);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
