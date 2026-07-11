package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FamiliarsRuseTest extends BaseCardTest {

    @Test
    @DisplayName("Casting returns a creature to hand and puts the spell on the stack targeting a spell")
    void castingReturnsCreatureAndTargetsSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent toReturn = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player2.getId()).add(toReturn);
        harness.setHand(player2, List.of(new FamiliarsRuse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, bears.getId(), toReturn.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(bears.getId());
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Resolving counters the target spell")
    void resolvingCountersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent toReturn = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player2.getId()).add(toReturn);
        harness.setHand(player2, List.of(new FamiliarsRuse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, bears.getId(), toReturn.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
        harness.assertInGraveyard(player2, "Familiar's Ruse");
    }

    @Test
    @DisplayName("Cannot cast without a creature to return")
    void cannotCastWithoutCreatureToReturn() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FamiliarsRuse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player2, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return an opponent's creature")
    void cannotReturnOpponentsCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent opponentCreature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(opponentCreature);
        harness.setHand(player2, List.of(new FamiliarsRuse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player2, 0, bears.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }
}
