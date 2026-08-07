package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlurenTest extends BaseCardTest {

    @Test
    @DisplayName("The controller casts a creature spell with mana value 3 or less without paying its mana cost")
    void controllerCastsSmallCreatureForFree() {
        harness.addToBattlefield(player1, new Aluren());
        // Grizzly Bears costs {1}{G} (mana value 2).
        harness.setHand(player1, List.of(new GrizzlyBears()));
        // No mana added — the spell must still be castable.

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("The free cast spends no mana")
    void freeCastSpendsNoMana() {
        harness.addToBattlefield(player1, new Aluren());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent may also cast a small creature spell for free")
    void opponentCastsSmallCreatureForFree() {
        harness.addToBattlefield(player1, new Aluren());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        // No mana for player2 — Aluren belongs to player1 but applies to any player.

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("A creature spell with mana value 4 is not free")
    void largeCreatureIsNotFree() {
        harness.addToBattlefield(player1, new Aluren());
        // Hill Giant costs {3}{R} (mana value 4), above the cap.
        harness.setHand(player1, List.of(new HillGiant()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A cheap noncreature spell is not free")
    void noncreatureSpellIsNotFree() {
        harness.addToBattlefield(player1, new Aluren());
        // Opt costs {U} (mana value 1) but is an instant, not a creature.
        harness.setHand(player1, List.of(new Opt()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The free cast is repeatable — it is not limited to once each turn")
    void freeCastIsNotLimitedPerTurn() {
        harness.addToBattlefield(player1, new Aluren());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("A small creature spell can be cast at instant speed")
    void smallCreatureHasFlashTiming() {
        harness.addToBattlefield(player1, new Aluren());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent can cast a small creature spell during the controller's turn")
    void opponentHasFlashTimingDuringControllersTurn() {
        harness.addToBattlefield(player1, new Aluren());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A creature spell above the mana value cap keeps sorcery timing")
    void largeCreatureKeepsSorceryTiming() {
        harness.addToBattlefield(player1, new Aluren());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
