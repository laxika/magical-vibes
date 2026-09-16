package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AlchemistsApprentice;
import com.github.laxika.magicalvibes.cards.i.ImplementsOfSacrifice;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DargoTheShipwrecker.class, GrizzlyBears.class, ImplementsOfSacrifice.class,
        AlchemistsApprentice.class, ZuranOrb.class, Forest.class})
class DargoTheShipwreckerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing artifacts and creatures reduces the cost by 2 each")
    void sacrificesArtifactsAndCreaturesForCostReduction() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ImplementsOfSacrifice());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DargoTheShipwrecker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreatureWithSacrificeForReduction(player1, 0, null,
                List.of(artifact.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dargo, the Shipwrecker");
        harness.assertInGraveyard(player1, "Implements of Sacrifice");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Earlier creature sacrifices reduce the cost, while earlier land sacrifices do not")
    void countsEarlierArtifactsOrCreaturesOnly() {
        harness.addToBattlefield(player1, new AlchemistsApprentice());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DargoTheShipwrecker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // The earlier creature makes Dargo cost {4}{R}; the earlier Forest must not count.
        assertThatThrownBy(() ->
                harness.castCreatureWithSacrificeForReduction(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only artifacts and creatures can be sacrificed for the cost reduction")
    void rejectsOtherPermanentTypes() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new DargoTheShipwrecker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreatureWithSacrificeForReduction(
                player1, 0, null, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not eligible to reduce this spell's cost");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
    }
}
