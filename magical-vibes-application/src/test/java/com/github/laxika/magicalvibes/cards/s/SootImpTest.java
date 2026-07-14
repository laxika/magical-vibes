package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Facevaulter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SootImpTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casting a nonblack spell makes that controller lose 1 life")
    void controllerCastsNonblackSpell() {
        harness.addToBattlefield(player1, new SootImp());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        // Mandatory trigger sits on the stack above the creature spell.
        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Soot Imp"));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The casting opponent — not Soot Imp's controller — loses the life")
    void opponentCastsNonblackSpell() {
        harness.addToBattlefield(player1, new SootImp());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int controllerLifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int casterLifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // trigger
        harness.passBothPriorities(); // creature spell

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(casterLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("A colorless spell is nonblack and still triggers the life loss")
    void colorlessSpellTriggers() {
        harness.addToBattlefield(player1, new SootImp());
        harness.setHand(player1, List.of(new Ornithopter()));

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Casting a black spell does not trigger Soot Imp")
    void blackSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SootImp());
        harness.setHand(player1, List.of(new Facevaulter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Soot Imp"));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
