package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Badlands;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathWard.class, FyndhornElves.class, IcyManipulator.class, Badlands.class, GrizzlyBears.class, SerraAngel.class})
class DeathWardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Death Ward grants a regeneration shield to target creature")
    void resolvingGrantsRegenerationShield() {
        harness.addToBattlefield(player1, new FyndhornElves());
        harness.setHand(player1, List.of(new DeathWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID elfId = harness.getPermanentId(player1, "Fyndhorn Elves");
        harness.castAndResolveInstant(player1, 0, elfId);

        Permanent elf = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(elf.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield from Death Ward saves creature from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        harness.addToBattlefield(player1, new FyndhornElves());
        harness.setHand(player1, List.of(new DeathWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID elfId = harness.getPermanentId(player1, "Fyndhorn Elves");
        harness.castAndResolveInstant(player1, 0, elfId);

        Permanent elf = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        elf.setBlocking(true);
        elf.addBlockingTarget(0);

        Permanent attacker = new Permanent(new FyndhornElves());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        Permanent survivedElf = findPermanent(player1, "Fyndhorn Elves");
        assertThat(survivedElf.isTapped()).isTrue();
        assertThat(survivedElf.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Death Ward can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new FyndhornElves());
        harness.setHand(player1, List.of(new DeathWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID elfId = harness.getPermanentId(player2, "Fyndhorn Elves");
        harness.castAndResolveInstant(player1, 0, elfId);

        Permanent elf = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(elf.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Death Ward fizzles if the target creature is removed")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new FyndhornElves());
        harness.setHand(player1, List.of(new DeathWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID elfId = harness.getPermanentId(player1, "Fyndhorn Elves");
        harness.castInstant(player1, 0, elfId);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Death Ward")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.setHand(player1, List.of(new DeathWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Icy Manipulator");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
