package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EsperSojournersTest extends BaseCardTest {

    // ===== Death trigger: you may tap or untap target permanent =====

    @Test
    @DisplayName("When it dies, taps an untapped target permanent")
    void diesTapsUntappedPermanent() {
        harness.addToBattlefield(player1, new EsperSojourners());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        killWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("When it dies, untaps a tapped target permanent")
    void diesUntapsTappedPermanent() {
        harness.addToBattlefield(player1, new EsperSojourners());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        killWithFlameJavelin();

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Death trigger can target any permanent, including non-creatures")
    void deathTargetsAnyPermanent() {
        harness.addToBattlefield(player1, new EsperSojourners());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(forest.getId(), bears.getId());
    }

    // ===== Cycling reflexive trigger: you may tap or untap target permanent, then draw =====

    @Test
    @DisplayName("Cycling taps target permanent and draws a card")
    void cyclingTapsTargetAndDraws() {
        harness.setHand(player1, List.of(new EsperSojourners()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        // The cycling draw still happens: Esper Sojourners discarded, the library card drawn.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Esper Sojourners"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private void killWithFlameJavelin() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID esperId = harness.getPermanentId(player1, "Esper Sojourners");
        harness.castInstant(player2, 0, esperId);
        harness.passBothPriorities(); // Flame Javelin resolves → Esper dies → death trigger awaits target
    }
}
