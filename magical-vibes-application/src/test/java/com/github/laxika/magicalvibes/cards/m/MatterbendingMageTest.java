package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatterbendingMageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one other target creature to its owner's hand")
    void etbReturnsAnotherCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new MatterbendingMage()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Matterbending Mage");
    }

    @Test
    @DisplayName("ETB can resolve without a target")
    void etbCanResolveWithoutTarget() {
        harness.setHand(player1, List.of(new MatterbendingMage()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Matterbending Mage");
    }

    @Test
    @DisplayName("Casting a spell with X in its mana cost makes the Mage unblockable this turn")
    void xSpellMakesMageUnblockable() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new MatterbendingMage());
        Blaze blaze = new Blaze();
        harness.setHand(player1, List.of(blaze));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(mage.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A spell without X does not make the Mage unblockable")
    void nonXSpellDoesNotTrigger() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new MatterbendingMage());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(mage.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The temporary unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new MatterbendingMage());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(mage.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mage.isCantBeBlocked()).isFalse();
    }
}
