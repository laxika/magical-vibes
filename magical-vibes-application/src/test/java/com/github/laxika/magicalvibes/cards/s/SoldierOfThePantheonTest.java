package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloryscaleViashino;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoldierOfThePantheonTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from multicolored sources")
    void hasProtectionFromMulticoloredSources() {
        Permanent soldier = addCreatureReady(player1, new SoldierOfThePantheon());
        Permanent multicoloredSource = addCreatureReady(player2, new GloryscaleViashino());
        Permanent monocoloredSource = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasProtectionFromSource(gd, soldier, multicoloredSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, soldier, monocoloredSource)).isFalse();
    }

    @Test
    @DisplayName("Gains 1 life when an opponent casts a multicolored spell")
    void gainsLifeWhenOpponentCastsMulticoloredSpell() {
        addCreatureReady(player1, new SoldierOfThePantheon());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new GloryscaleViashino()));
        addGloryscaleViashinoMana(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's monocolored spell")
    void doesNotTriggerForOpponentMonocoloredSpell() {
        addCreatureReady(player1, new SoldierOfThePantheon());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger for your own multicolored spell")
    void doesNotTriggerForOwnMulticoloredSpell() {
        addCreatureReady(player1, new SoldierOfThePantheon());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new GloryscaleViashino()));
        addGloryscaleViashinoMana(player1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addGloryscaleViashinoMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }
}
