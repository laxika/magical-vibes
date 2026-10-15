package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SymmetryMatrix.class, GrizzlyBears.class, EliteVanguard.class, MarchOfTheMachines.class})
class SymmetryMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} draws a card when a creature with equal power and toughness enters")
    void payingOneManaDrawsCardForEqualStatsCreature() {
        harness.addToBattlefield(player1, new SymmetryMatrix());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for a creature with unequal power and toughness")
    void doesNotTriggerForUnequalStatsCreature() {
        harness.addToBattlefield(player1, new SymmetryMatrix());
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the payment does not draw a card")
    void decliningPaymentDoesNotDrawCard() {
        harness.addToBattlefield(player1, new SymmetryMatrix());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SymmetryMatrix());
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void changingStatsAfterEntryDoesNotPreventDrawing() {
        harness.addToBattlefield(player1, new SymmetryMatrix());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new SymmetryMatrix()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst().orElseThrow().setPowerModifier(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Symmetry Matrix");
    }

    @Test
    void creatureLeavingAfterEntryDoesNotPreventDrawing() {
        harness.addToBattlefield(player1, new SymmetryMatrix());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new SymmetryMatrix()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof GrizzlyBears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Symmetry Matrix");
    }

    @Test
    void triggersForItsOwnEntryWhenAnimatedByMarchOfTheMachines() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.setHand(player1, List.of(new SymmetryMatrix()));
        harness.setLibrary(player1, List.of(new SymmetryMatrix()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Symmetry Matrix");
    }
}
