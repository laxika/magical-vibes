package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarhornBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving boosts creatures you control by +2/+1")
    void resolvingBoostsOwnCreatures() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WarhornBlast()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownBears.getPowerModifier()).isEqualTo(2);
        assertThat(ownBears.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentBears.getPowerModifier()).isZero();
        assertThat(opponentBears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost ends at cleanup")
    void boostEndsAtCleanup() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WarhornBlast()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownBears.getPowerModifier()).isZero();
        assertThat(ownBears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can be foretold and cast for {2}{W} on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        WarhornBlast blast = new WarhornBlast();
        harness.setHand(player1, List.of(blast));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(blast.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, blast.getId());
        harness.passBothPriorities();

        assertThat(ownBears.getPowerModifier()).isEqualTo(2);
        assertThat(ownBears.getToughnessModifier()).isEqualTo(1);
    }
}
