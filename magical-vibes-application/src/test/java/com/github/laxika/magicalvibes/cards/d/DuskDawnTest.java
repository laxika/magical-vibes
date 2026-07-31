package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuskDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Dusk destroys creatures with power 3 or greater and spares weaker ones")
    void duskDestroysPower3OrGreater() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DuskDawn()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Dusk");
    }

    @Test
    @DisplayName("Dawn returns power-2-or-less creature cards from graveyard to hand, then exiles")
    void dawnReturnsSmallCreaturesAndExiles() {
        harness.setGraveyard(player1, List.of(
                new DuskDawn(),
                new FugitiveWizard(),
                new GrizzlyBears(),
                new HillGiant()
        ));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInHand(player1, "Fugitive Wizard");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Dusk") || c.getName().equals("Dawn"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dusk"));
    }

    @Test
    @DisplayName("Dawn requires sorcery timing")
    void dawnRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new DuskDawn()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    @DisplayName("Dawn fails without enough mana")
    void dawnFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new DuskDawn()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
