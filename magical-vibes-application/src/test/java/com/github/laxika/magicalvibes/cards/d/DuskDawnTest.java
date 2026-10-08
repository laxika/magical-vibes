package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskDawn.class, Dawn.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class})
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

        harness.castAndResolveSorcery(player1, 0, List.of());

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

        harness.castAndResolveFlashback(player1, 0, null);

        GameData gd = harness.getGameData();
        harness.assertInHand(player1, "Fugitive Wizard");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotInGraveyard(player1, "Dusk");
        harness.assertNotInGraveyard(player1, "Dawn");
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

    @Test
    @DisplayName("Dawn returns only creatures in its controller's graveyard")
    void dawnLeavesNoncreaturesAndOpponentsCards() {
        DuskDawn spell = new DuskDawn();
        DuskDawn otherSorcery = new DuskDawn();
        harness.setGraveyard(player1, List.of(spell, otherSorcery, new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new FugitiveWizard(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherSorcery);
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Fugitive Wizard");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Dawn resolves and exiles even with no creatures to return")
    void dawnResolvesWithNoEligibleCards() {
        DuskDawn spell = new DuskDawn();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Dusk uses current power, including counters, on both sides of the battlefield")
    void duskUsesCurrentPower() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears())
                .getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefieldAndReturn(player2, new HillGiant())
                .getCounters().put(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new DuskDawn()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }
}
