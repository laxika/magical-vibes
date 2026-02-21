package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RootMazeTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Root Maze has correct card properties")
    void hasCorrectProperties() {
        RootMaze card = new RootMaze();

        assertThat(card.getName()).isEqualTo("Root Maze");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(EnterPermanentsOfTypesTappedEffect.class);
        EnterPermanentsOfTypesTappedEffect effect = (EnterPermanentsOfTypesTappedEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.cardTypes()).containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.LAND);
        assertThat(card.getCardText())
                .containsIgnoringCase("artifact")
                .containsIgnoringCase("land")
                .containsIgnoringCase("tapped");
    }

    @Test
    @DisplayName("Lands enter tapped while Root Maze is on battlefield")
    void landsEnterTapped() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player1, List.of(new Forest()));

        gs.playCard(gd, player1, 0, 0, null, null);

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst()
                .orElseThrow();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Artifacts enter tapped for both players while Root Maze is on battlefield")
    void artifactsEnterTappedForBothPlayers() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        Permanent ornithopter = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ornithopter"))
                .findFirst()
                .orElseThrow();
        assertThat(ornithopter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-artifact non-land permanents are not tapped by Root Maze")
    void creaturesAreNotTappedByRootMaze() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Existing permanents are not tapped when Root Maze enters")
    void existingPermanentsAreNotTappedWhenRootMazeEnters() {
        Permanent existingForest = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(existingForest);

        harness.setHand(player1, List.of(new RootMaze()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(existingForest.isTapped()).isFalse();
    }
}
