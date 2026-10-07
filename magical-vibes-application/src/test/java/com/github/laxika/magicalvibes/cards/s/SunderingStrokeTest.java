package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunderingStroke.class, AirElemental.class, GrizzlyBears.class, HillGiant.class,
        Twincast.class, Unsummon.class})
class SunderingStrokeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 7 damage divided among one to three targets when fewer than seven red mana was spent")
    void dividesDamageWhenFewerThanSevenRedManaWasSpent() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, Map.of(
                bears.getId(), 2,
                giant.getId(), 3,
                player2.getId(), 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals 7 damage to each target when at least seven red mana was spent")
    void dealsFullDamageToEachTargetWhenSevenRedManaWasSpent() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, Map.of(
                bears.getId(), 1,
                elemental.getId(), 2,
                player2.getId(), 4));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    void sixRedManaStillUsesTheChosenDivision() {
        int lifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, Map.of(player1.getId(), 3, player2.getId(), 4));
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLifeBefore - 3);
        harness.assertLife(player2, lifeBefore - 4);
    }

    @Test
    void canAssignAllSevenDamageToOneTarget() {
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 7));
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 7);
    }

    @Test
    void sevenRedManaDoesNotAllowZeroDamageAssignments() {
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 7)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sevenRedManaStillRequiresAssignmentsToSumToSeven() {
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(player1.getId(), 7, player2.getId(), 7)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseFourTargetsEvenWithSevenRedMana() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(
                bears.getId(), 1, giant.getId(), 1, player1.getId(), 1, player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageAssignedToAnIllegalTargetIsNotRedistributed() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 5, player2.getId(), 2));
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void sevenRedManaStillDealsSevenToTheRemainingLegalTarget() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new SunderingStroke()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 5, player2.getId(), 2));
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 7);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void copyUsesTheOriginalDivisionEvenWhenTheOriginalWasCastWithSevenRedMana() {
        var stroke = new SunderingStroke();
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(stroke));
        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, Map.of(player1.getId(), 3, player2.getId(), 4));
        harness.castAndResolveInstant(player2, 0, stroke.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLifeBefore - 3);
        harness.assertLife(player2, opponentLifeBefore - 4);

        harness.passBothPriorities();

        harness.assertLife(player1, controllerLifeBefore - 10);
        harness.assertLife(player2, opponentLifeBefore - 11);
    }
}
