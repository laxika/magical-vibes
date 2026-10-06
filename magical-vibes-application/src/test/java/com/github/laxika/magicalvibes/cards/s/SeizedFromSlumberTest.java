package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BearTrap;
import com.github.laxika.magicalvibes.cards.c.CautiousSurvivor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeizedFromSlumber.class, CautiousSurvivor.class, BearTrap.class})
class SeizedFromSlumberTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a tapped creature for the reduced cost")
    void destroysTappedCreatureForReducedCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());
        target.tap();

        harness.setHand(player1, List.of(new SeizedFromSlumber()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Cautious Survivor");
        harness.assertInGraveyard(player2, "Cautious Survivor");
    }

    @Test
    @DisplayName("Destroys an untapped creature for the full cost")
    void destroysUntappedCreatureForFullCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        harness.setHand(player1, List.of(new SeizedFromSlumber()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Cautious Survivor");
        harness.assertInGraveyard(player2, "Cautious Survivor");
    }

    @Test
    @DisplayName("Cannot pay the reduced cost when targeting an untapped creature")
    void cannotPayReducedCostForUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        harness.setHand(player1, List.of(new SeizedFromSlumber()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BearTrap());

        harness.setHand(player1, List.of(new SeizedFromSlumber()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Reduced cost can be paid with one white and one colorless mana")
    void reducedCostAllowsColorlessManaForGenericPortion() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());
        target.tap();
        harness.setHand(player1, List.of(new SeizedFromSlumber()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Cautious Survivor");
    }

    @Test
    @DisplayName("Reduced cost still requires white mana")
    void reducedCostStillRequiresWhiteMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());
        target.tap();
        harness.setHand(player1, List.of(new SeizedFromSlumber()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Cautious Survivor");
        harness.assertInHand(player1, "Seized from Slumber");
    }

    @Test
    @DisplayName("Reduced cost still requires two mana")
    void oneWhiteManaIsInsufficientForTappedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());
        target.tap();
        harness.setHand(player1, List.of(new SeizedFromSlumber()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Seized from Slumber");
    }

    @Test
    @DisplayName("A tapped creature controlled by the caster also gives the reduction")
    void destroysOwnTappedCreatureForReducedCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        target.tap();
        harness.setHand(player1, List.of(new SeizedFromSlumber()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Cautious Survivor");
        harness.assertInGraveyard(player1, "Cautious Survivor");
    }

    @Test
    @DisplayName("Untapping the target after casting does not prevent destruction")
    void destroysTargetThatUntapsBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());
        target.tap();
        harness.setHand(player1, List.of(new SeizedFromSlumber()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cautious Survivor");
        harness.assertInGraveyard(player2, "Cautious Survivor");
    }

    @Test
    @DisplayName("An unrelated tapped creature does not reduce the cost")
    void tappedNonTargetDoesNotReduceCost() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());
        tappedCreature.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());
        harness.setHand(player1, List.of(new SeizedFromSlumber()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Cautious Survivor");
        harness.assertInHand(player1, "Seized from Slumber");
    }
}
