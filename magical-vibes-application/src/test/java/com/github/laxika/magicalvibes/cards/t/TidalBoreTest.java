package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TidalBoreTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target creature when accepted")
    void tapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castForMana(target);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped target creature when accepted")
    void untapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        castForMana(target);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the may leaves the target unchanged")
    void decliningMayDoesNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castForMana(target);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can be cast by returning an Island")
    void castsByReturningAnIsland() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TidalBore()));

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(island.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Tidal Bore");
    }

    @Test
    @DisplayName("Alternate cost rejects a non-Island")
    void alternateCostRejectsNonIsland() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TidalBore()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only creatures are legal targets")
    void onlyCreaturesAreLegalTargets() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TidalBore()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castForMana(Permanent target) {
        harness.setHand(player1, List.of(new TidalBore()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
