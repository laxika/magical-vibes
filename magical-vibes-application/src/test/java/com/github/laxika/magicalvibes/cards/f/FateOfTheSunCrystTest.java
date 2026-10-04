package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AdventurersAirship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FateOfTheSunCryst.class, GrizzlyBears.class, Forest.class, AdventurersAirship.class})
class FateOfTheSunCrystTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonland permanent")
    void destroysTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FateOfTheSunCryst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Costs less when targeting a tapped creature")
    void costsLessWhenTargetingTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        harness.setHand(player1, List.of(new FateOfTheSunCryst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new FateOfTheSunCryst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    void untappedCreatureDoesNotReceiveDiscountEvenWithAnotherTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).tap();
        harness.setHand(player1, List.of(new FateOfTheSunCryst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Fate of the Sun-Cryst");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void tappedNoncreatureDoesNotReceiveDiscount() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AdventurersAirship());
        target.tap();
        harness.setHand(player1, List.of(new FateOfTheSunCryst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Fate of the Sun-Cryst");
        harness.assertOnBattlefield(player2, "Adventurer's Airship");
    }

    @Test
    void destroysNoncreaturePermanentAtFullCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AdventurersAirship());
        harness.setHand(player1, List.of(new FateOfTheSunCryst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Adventurer's Airship");
        harness.assertInGraveyard(player2, "Adventurer's Airship");
    }

    @Test
    void canDestroyOwnTappedCreatureAtReducedCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new FateOfTheSunCryst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void untappingTargetAfterCastingDoesNotUndoDiscountOrPreventDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new FateOfTheSunCryst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void discountDoesNotRemoveWhiteManaRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new FateOfTheSunCryst()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Fate of the Sun-Cryst");
    }
}
