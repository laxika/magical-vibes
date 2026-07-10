package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HealTheScarsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Heal the Scars targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving grants a regeneration shield and gains life equal to the creature's toughness")
    void resolvingGrantsShieldAndGainsLife() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        // Grizzly Bears is 2/2 -> gain 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Life gained scales with the target creature's toughness")
    void lifeGainScalesWithToughness() {
        Permanent giant = new Permanent(new HillGiant());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(giant);

        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Hill Giant is 3/3 -> gain 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Regeneration shield saves the target from lethal combat damage")
    void shieldSavesFromLethalCombatDamage() {
        // Grizzly Bears (2/2) blocks a Hill Giant (3/3) -> would die, but is regenerated first
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.getRegenerationShield()).isEqualTo(1);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(bears.getRegenerationShield()).isEqualTo(0);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.isBlocking()).isFalse();
    }
}
