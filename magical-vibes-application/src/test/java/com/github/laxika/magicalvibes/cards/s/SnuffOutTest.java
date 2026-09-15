package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogWitch;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnuffOut.class, Swamp.class, FreshVolunteers.class, BogWitch.class})
class SnuffOutTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature and costs 4 life when cast for its alternate cost")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SnuffOut()));

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player2, "Fresh Volunteers");
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Can be cast normally with mana")
    void castsNormally() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new SnuffOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The alternate cost requires control of a Swamp")
    void alternateCostRequiresSwamp() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new SnuffOut()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BogWitch());
        harness.setHand(player1, List.of(new SnuffOut()));
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new SnuffOut()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Has no effect if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new SnuffOut()));
        harness.setLife(player1, 20);

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertNotInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Destroys a creature even if it has a regeneration shield")
    void cannotBeRegenerated() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new SnuffOut()));
        harness.setLife(player1, 20);

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }
}
