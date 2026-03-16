package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PistusStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Pistus Strike targeting a creature with flying puts it on stack")
    void castingPutsOnStack() {
        Permanent angel = new Permanent(new SerraAngel());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(angel);

        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, angel.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Pistus Strike");
        assertThat(entry.getTargetPermanentId()).isEqualTo(angel.getId());
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        // Add a creature with flying as valid target so spell is playable
        harness.getGameData().playerBattlefields.get(player1.getId()).add(new Permanent(new SerraAngel()));

        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Resolving Pistus Strike destroys target creature and gives controller a poison counter")
    void resolvingDestroysAndGivesPoison() {
        Permanent angel = new Permanent(new SerraAngel());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(angel);

        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature is destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
        // Controller gets a poison counter
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        // Caster does NOT get a poison counter
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        // Pistus Strike goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pistus Strike"));
    }

    @Test
    @DisplayName("Pistus Strike fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent angel = new Permanent(new SerraAngel());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(angel);

        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, angel.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // No poison counter since spell fizzled
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        // Pistus Strike goes to graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pistus Strike"));
    }
}
