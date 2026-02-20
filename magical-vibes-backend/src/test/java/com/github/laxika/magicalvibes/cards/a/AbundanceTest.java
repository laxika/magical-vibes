package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AbundanceDrawReplacementEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbundanceTest {

    private GameTestHarness harness;
    private GameService gameService;
    private Player player1;
    private Player player2;
    private GameData gameData;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        gameService = harness.getGameService();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gameData = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Abundance has correct card properties")
    void hasCorrectProperties() {
        Abundance card = new Abundance();

        assertThat(card.getName()).isEqualTo("Abundance");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{2}{G}{G}");
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(AbundanceDrawReplacementEffect.class);
    }

    @Test
    @DisplayName("Can replace draw step draw with nonland card")
    void replacesDrawStepDrawWithNonland() {
        harness.addToBattlefield(player1, new Abundance());
        gameData.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        )));

        harness.forceActivePlayer(player1);
        gameData.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gameData.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gameData.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);

        harness.handleColorChosen(player1, "NONLAND");

        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gameData.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Island");
        assertThat(gameData.playerDecks.get(player1.getId()).getLast().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Declining Abundance uses a normal draw")
    void decliningAbundanceDrawsNormally() {
        harness.addToBattlefield(player1, new Abundance());
        gameData.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears()
        )));

        harness.forceActivePlayer(player1);
        gameData.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gameData.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gameData.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing land can require bottom reorder for revealed nonlands")
    void choosingLandCanRequireBottomReorder() {
        harness.addToBattlefield(player1, new Abundance());
        gameData.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new GrizzlyBears(),
                new Peek(),
                new Forest(),
                new Island()
        )));

        harness.forceActivePlayer(player1);
        gameData.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleColorChosen(player1, "LAND");

        assertThat(gameData.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        gameService.handleLibraryCardsReordered(gameData, player1, List.of(1, 0));

        assertThat(gameData.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Island");
        assertThat(gameData.playerDecks.get(player1.getId()).get(1).getName()).isEqualTo("Peek");
        assertThat(gameData.playerDecks.get(player1.getId()).get(2).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Each draw from draw-two effect gets its own Abundance choice")
    void drawTwoPromptsPerDraw() {
        harness.addToBattlefield(player1, new Abundance());
        gameData.playerDecks.put(player1.getId(), new ArrayList<>(List.of(
                new Forest(),
                new GrizzlyBears(),
                new Island()
        )));
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gameData.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gameData.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        harness.handleColorChosen(player1, "NONLAND");

        assertThat(gameData.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Island"));
    }
}
