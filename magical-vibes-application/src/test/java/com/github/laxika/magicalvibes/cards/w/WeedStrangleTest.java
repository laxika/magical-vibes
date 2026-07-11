package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeedStrangleTest extends BaseCardTest {

    private Permanent prepareTargetCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2, toughness 2
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new WeedStrangle()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // {3}{B}{B}

        harness.setLife(player1, 20);
        return bears;
    }

    // Caster (player1) wins: their revealed top card has a strictly greater mana value.
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // ===== Won clash → destroy + gain life equal to the creature's toughness =====

    @Test
    @DisplayName("Winning the clash destroys the creature and gains life equal to its toughness")
    void wonClashDestroysAndGainsLife() {
        Permanent bears = prepareTargetCreature();
        stackClashWinForCaster();

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22); // 20 + toughness 2
    }

    // ===== Lost clash → destroy but no life gain =====

    @Test
    @DisplayName("Losing the clash still destroys the creature but gains no life")
    void lostClashDestroysButGainsNoLife() {
        Permanent bears = prepareTargetCreature();
        // Player1 loses: player2 reveals the strictly greater mana value.
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20); // unchanged
    }

    // ===== Tie → destroy but no life gain (win requires strictly greater mana value) =====

    @Test
    @DisplayName("A tied clash destroys the creature but gains no life")
    void tiedClashGainsNoLife() {
        Permanent bears = prepareTargetCreature();
        // Equal mana values on top (both Grizzly Bears MV 2) → not a win.
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20); // unchanged
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        prepareTargetCreature();

        Permanent land = new Permanent(new Forest());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(land);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
