package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagebaneLizard.class, GrizzlyBears.class, HolyDay.class})
class MagebaneLizardTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToNoncreatureSpellsCastThisTurn() {
        harness.addToBattlefield(player1, new MagebaneLizard());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new HolyDay(), new HolyDay()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);
        resolveAllStack();
        harness.castInstant(player2, 0);
        resolveAllStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void countsOnlyNoncreatureSpells() {
        harness.addToBattlefield(player1, new MagebaneLizard());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new GrizzlyBears(), new HolyDay()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllStack();
        harness.castInstant(player2, 0);
        resolveAllStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void alsoTriggersWhenItsControllerCastsANoncreatureSpell() {
        harness.addToBattlefield(player1, new MagebaneLizard());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        resolveAllStack();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void resolveAllStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
