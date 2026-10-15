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

        harness.castFromHand(player1, new SerraAngel(), "{2}{W}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void chosenAbilityWordReducesMatchingSpells() {
        putAntelopeOnBattlefieldAfterChoosing("LANDFALL");

        harness.castFromHand(player1, new LotusCobra(), "{G}");
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

    @Test
    void legacyChoiceIsRequiredForAntelopeStartingInLibrary() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        InspirationalAntelope antelope = new InspirationalAntelope();
        openingHarness.setHand(openingPlayer, List.of());
        openingHarness.setHand(openingHarness.getPlayer2(), List.of());
        openingHarness.setLibrary(openingPlayer, List.of(antelope));

        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();
        if (openingHarness.getGameData().interaction.activeInteraction()
                instanceof PendingInteraction.MayAbilityChoice) {
            openingHarness.handleMayAbilityChosen(openingPlayer, true);
        }
        openingHarness.handleListChoice(openingPlayer, "FLYING");

        assertThat(openingHarness.getGameData().legacyChosenWordsByCardId)
                .containsEntry(antelope.getId(), "FLYING");
    }

    @Test
    void cyclingIsALegalLegacyKeywordChoice() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        InspirationalAntelope antelope = new InspirationalAntelope();
        openingHarness.setHand(openingPlayer, List.of(antelope));
        openingHarness.skipMulligan();
        openingHarness.handleMayAbilityChosen(openingPlayer, true);

        openingHarness.handleListChoice(openingPlayer, "CYCLING");

        assertThat(openingHarness.getGameData().legacyChosenWordsByCardId)
                .containsEntry(antelope.getId(), "CYCLING");
    }

    @Test
    void keywordActionsCannotBeChosenForLegacy() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        openingHarness.setHand(openingPlayer, List.of(new InspirationalAntelope()));
        openingHarness.skipMulligan();
        openingHarness.handleMayAbilityChosen(openingPlayer, true);

        assertThatThrownBy(() -> openingHarness.handleListChoice(openingPlayer, "SCRY"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chosenWordDoesNotReduceSpellsWhileAntelopeIsOnlyInHand() {
        InspirationalAntelope antelope = new InspirationalAntelope();
        gd.legacyChosenWordsByCardId.put(antelope.getId(), "FLYING");
        harness.setHand(player1, List.of(antelope, new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void reductionCannotPayColoredMana() {
        putAntelopeOnBattlefieldAfterChoosing("FLYING");
        harness.setHand(player1, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void multipleAntelopesReduceTheSameSpellIndependently() {
        putAntelopeOnBattlefieldAfterChoosing("FLYING");
        putAntelopeOnBattlefieldAfterChoosing("FLYING");

        harness.castFromHand(player1, new SerraAngel(), "{1}{W}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void gameCannotStartWithAnUnchosenLegacyWord() {
        GameTestHarness openingHarness = new GameTestHarness();
        Player openingPlayer = openingHarness.getPlayer1();
        InspirationalAntelope antelope = new InspirationalAntelope();
        openingHarness.setHand(openingPlayer, List.of(antelope));
        openingHarness.skipMulligan();

        if (openingHarness.getGameData().interaction.activeInteraction()
                instanceof PendingInteraction.MayAbilityChoice) {
            openingHarness.handleMayAbilityChosen(openingPlayer, false);
        }

        assertThat(openingHarness.getGameData().status).isNotEqualTo(GameStatus.RUNNING);
        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void antelopeDoesNotReduceOpponentsSpells() {
        putAntelopeOnBattlefieldAfterChoosing("FLYING");
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SerraAngel()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void putAntelopeOnBattlefieldAfterChoosing(String chosenWord) {
        InspirationalAntelope antelope = new InspirationalAntelope();
        // BaseCardTest has already completed the opening-hand procedure. Its interactive
        // choice is covered separately by openingHandChoiceStoresTheChosenKeyword.
        gd.legacyChosenWordsByCardId.put(antelope.getId(), chosenWord);
        harness.castFromHand(player1, antelope, "{1}{G}");
        harness.passBothPriorities();
    }
}
