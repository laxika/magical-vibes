package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChefsKiss.class, Counterspell.class, GrizzlyBears.class, IvoryMask.class, Shock.class})
class ChefsKissTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a spell and randomly retargets both objects away from its controller")
    void copiesAndRandomlyRetargetsSpell() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChefsKiss()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        StackEntry original = gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(shock.getId()))
                .findFirst()
                .orElseThrow();
        StackEntry copy = gd.stack.stream()
                .filter(StackEntry::isCopy)
                .findFirst()
                .orElseThrow();

        assertThat(original.getControllerId()).isEqualTo(player1.getId());
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());
        assertThat(original.getTargetId()).isEqualTo(player2.getId());
        assertThat(copy.getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Leaves targets unchanged when no permitted random target exists")
    void leavesTargetsUnchangedWithoutPermittedTarget() {
        harness.addToBattlefield(player2, new IvoryMask());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new ChefsKiss()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(shock.getId()))
                .findFirst().orElseThrow().getTargetId()).isEqualTo(player1.getId());
        assertThat(gd.stack.stream()
                .filter(StackEntry::isCopy)
                .findFirst().orElseThrow().getTargetId()).isEqualTo(player1.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a spell whose only target is another spell")
    void rejectsSpellTarget() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, shock.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new ChefsKiss()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, counterspell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single permanent or player");
    }
}
