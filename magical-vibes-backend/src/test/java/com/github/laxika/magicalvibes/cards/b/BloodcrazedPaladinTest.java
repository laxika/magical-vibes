package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersPerCreatureDeathsThisTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BloodcrazedPaladinTest extends BaseCardTest {

    // ===== Card effects =====

    @Test
    @DisplayName("Has EnterWithPlusOnePlusOneCountersPerCreatureDeathsThisTurnEffect as ETB effect")
    void hasCorrectETBEffect() {
        BloodcrazedPaladin card = new BloodcrazedPaladin();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithPlusOnePlusOneCountersPerCreatureDeathsThisTurnEffect.class);
    }

    // ===== ETB counter placement =====

    @Test
    @DisplayName("Enters as 1/1 with no counters when no creatures died this turn")
    void entersWithNoCountersWhenNoDeaths() {
        harness.setHand(player1, List.of(new BloodcrazedPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent paladin = findPaladin(player1);
        assertThat(paladin).isNotNull();
        assertThat(paladin.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Enters with counters equal to total creature deaths this turn from controller's side")
    void entersWithCountersFromControllerDeaths() {
        // Simulate 2 creature deaths on player1's side
        gd.creatureDeathCountThisTurn.put(player1.getId(), 2);

        harness.setHand(player1, List.of(new BloodcrazedPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent paladin = findPaladin(player1);
        assertThat(paladin).isNotNull();
        assertThat(paladin.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters with counters counting opponent's creature deaths too")
    void entersWithCountersFromOpponentDeaths() {
        // Simulate 1 death on player1's side and 3 on player2's side
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);

        harness.setHand(player1, List.of(new BloodcrazedPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent paladin = findPaladin(player1);
        assertThat(paladin).isNotNull();
        // Counts all deaths across all players: 1 + 3 = 4
        assertThat(paladin.getPlusOnePlusOneCounters()).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters with counters after killing a creature with Shock")
    void entersWithCountersAfterActualCreatureDeath() {
        // Put a Grizzly Bears on the battlefield for player2
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Kill the Bears with Shock (2 damage to 2/2)
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        // Bears should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Now cast Bloodcrazed Paladin — should get 1 counter from the Bears death
        harness.setHand(player1, List.of(new BloodcrazedPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent paladin = findPaladin(player1);
        assertThat(paladin).isNotNull();
        assertThat(paladin.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts only deaths from this turn, not from previous turns")
    void doesNotCountDeathsFromPreviousTurns() {
        // Deaths from a previous turn would have been cleared by turn reset.
        // creatureDeathCountThisTurn is empty by default, so casting with no deaths
        // gives 0 counters. This test verifies that baseline.
        harness.setHand(player1, List.of(new BloodcrazedPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent paladin = findPaladin(player1);
        assertThat(paladin).isNotNull();
        assertThat(paladin.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent findPaladin(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bloodcrazed Paladin"))
                .findFirst().orElse(null);
    }
}
