package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchmagesCharm.class, GrizzlyBears.class, HillGiant.class, Island.class, LlanowarElves.class})
class ArchmagesCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode counters a target spell")
    void countersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new ArchmagesCharm()));
        addBlueMana(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draw mode makes the target player draw two cards")
    void targetPlayerDrawsTwoCards() {
        harness.setHand(player1, List.of(new ArchmagesCharm()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new HillGiant()));
        addBlueMana(player1);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Control mode permanently gains control of an eligible permanent")
    void gainsControlOfLowManaValueNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new ArchmagesCharm()));
        addBlueMana(player1);

        harness.castInstant(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentController(gd, target.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Control mode rejects a permanent with mana value greater than one")
    void controlModeRejectsHighManaValuePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ArchmagesCharm()));
        addBlueMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Control mode rejects a land")
    void controlModeRejectsLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ArchmagesCharm()));
        addBlueMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBlueMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLUE, 3);
    }
}
