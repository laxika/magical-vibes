package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.d.DolmenGate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FamiliarsRuse.class, AvianChangeling.class, DolmenGate.class})
class FamiliarsRuseTest extends BaseCardTest {

    @Test
    @DisplayName("Casting returns a creature to hand and puts the spell on the stack targeting a spell")
    void castingReturnsCreatureAndTargetsSpell() {
        AvianChangeling target = new AvianChangeling();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent toReturn = harness.addToBattlefieldAndReturn(player2, new AvianChangeling());
        harness.setHand(player2, List.of(new FamiliarsRuse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, target.getId(), toReturn.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(target.getId());
        harness.assertNotOnBattlefield(player2, "Avian Changeling");
        harness.assertInHand(player2, "Avian Changeling");
    }

    @Test
    @DisplayName("Resolving counters the target spell")
    void resolvingCountersTargetSpell() {
        AvianChangeling target = new AvianChangeling();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent toReturn = harness.addToBattlefieldAndReturn(player2, new AvianChangeling());
        harness.setHand(player2, List.of(new FamiliarsRuse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, target.getId(), toReturn.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Avian Changeling");
        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Avian Changeling"));
        harness.assertInGraveyard(player2, "Familiar's Ruse");
    }

    @Test
    @DisplayName("Cannot cast without a creature to return")
    void cannotCastWithoutCreatureToReturn() {
        AvianChangeling target = new AvianChangeling();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new FamiliarsRuse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player2, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return an opponent's creature")
    void cannotReturnOpponentsCreature() {
        AvianChangeling target = new AvianChangeling();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());
        harness.setHand(player2, List.of(new FamiliarsRuse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player2, 0, target.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Cannot return a noncreature permanent")
    void cannotReturnNoncreaturePermanent() {
        AvianChangeling target = new AvianChangeling();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DolmenGate());
        harness.setHand(player2, List.of(new FamiliarsRuse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player2, 0, target.getId(), artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }
}
