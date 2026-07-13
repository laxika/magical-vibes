package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpellBlastTest extends BaseCardTest {

    // ===== Targeting =====

    @Test
    @DisplayName("Can target a spell whose mana value equals X")
    void canTargetSpellWithManaValueEqualToX() {
        GrizzlyBears bears = new GrizzlyBears(); // mana value 2
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SpellBlast()));
        harness.addMana(player2, ManaColor.BLUE, 3); // X=2 + {U}

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId()); // X = 2

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry blastEntry = gd.stack.getLast();
        assertThat(blastEntry.getCard().getName()).isEqualTo("Spell Blast");
        assertThat(blastEntry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a spell whose mana value differs from X")
    void cannotTargetSpellWithDifferentManaValue() {
        GrizzlyBears bears = new GrizzlyBears(); // mana value 2
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SpellBlast()));
        harness.addMana(player2, ManaColor.BLUE, 2); // X=1 + {U}

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, bears.getId())) // X = 1
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters the targeted spell whose mana value equals X")
    void countersSpellWhenXMatches() {
        LlanowarElves elves = new LlanowarElves(); // mana value 1
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new SpellBlast()));
        harness.addMana(player2, ManaColor.BLUE, 2); // X=1 + {U}

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, elves.getId()); // X = 1
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spell Blast"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if the target spell is no longer on the stack")
    void fizzlesIfTargetRemoved() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new SpellBlast()));
        harness.addMana(player2, ManaColor.BLUE, 2); // X=1 + {U}

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, elves.getId()); // X = 1

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Llanowar Elves"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spell Blast"));
    }
}
