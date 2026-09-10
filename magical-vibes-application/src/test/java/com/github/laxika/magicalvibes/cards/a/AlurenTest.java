package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CanyonWildcat;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GiantCrab;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aluren.class, CanyonWildcat.class, DarkRitual.class, GiantCrab.class, WindDrake.class})
class AlurenTest extends BaseCardTest {

    @Test
    @DisplayName("The controller casts a creature spell with mana value 3 or less without paying its mana cost")
    void controllerCastsSmallCreatureForFree() {
        harness.addToBattlefield(player1, new Aluren());
        // Canyon Wildcat costs {1}{R} (mana value 2).
        CanyonWildcat creature = new CanyonWildcat();
        harness.setHand(player1, List.of(creature));
        // No mana added — the spell must still be castable.

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(creature);
    }

    @Test
    @DisplayName("The free cast spends no mana")
    void freeCastSpendsNoMana() {
        harness.addToBattlefield(player1, new Aluren());
        harness.setHand(player1, List.of(new CanyonWildcat()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent may also cast a small creature spell for free")
    void opponentCastsSmallCreatureForFree() {
        harness.addToBattlefield(player1, new Aluren());
        CanyonWildcat creature = new CanyonWildcat();
        harness.setHand(player2, List.of(creature));
        // No mana for player2 — Aluren belongs to player1 but applies to any player.

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(creature);
    }

    @Test
    @DisplayName("A creature spell above mana value 3 is not free")
    void largeCreatureIsNotFree() {
        harness.addToBattlefield(player1, new Aluren());
        // Giant Crab costs {4}{U} (mana value 5), above the cap.
        harness.setHand(player1, List.of(new GiantCrab()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A cheap noncreature spell is not free")
    void noncreatureSpellIsNotFree() {
        harness.addToBattlefield(player1, new Aluren());
        // Dark Ritual costs {B} (mana value 1) but is an instant, not a creature.
        harness.setHand(player1, List.of(new DarkRitual()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The free cast is repeatable — it is not limited to once each turn")
    void freeCastIsNotLimitedPerTurn() {
        harness.addToBattlefield(player1, new Aluren());
        harness.setHand(player1, List.of(new CanyonWildcat(), new CanyonWildcat()));

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
        harness.setHand(player1, List.of(new CanyonWildcat()));

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
        harness.setHand(player2, List.of(new CanyonWildcat()));

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A creature spell above the mana value cap keeps sorcery timing")
    void largeCreatureKeepsSorceryTiming() {
        harness.addToBattlefield(player1, new Aluren());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GiantCrab()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature spell with mana value exactly 3 is free and has flash")
    void creatureWithManaValueThreeIsFreeAndHasFlash() {
        harness.addToBattlefield(player1, new Aluren());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        WindDrake creature = new WindDrake();
        harness.setHand(player1, List.of(creature));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(creature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
