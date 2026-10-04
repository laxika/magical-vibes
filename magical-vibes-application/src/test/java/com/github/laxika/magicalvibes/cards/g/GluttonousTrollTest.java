package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GluttonousTroll.class, GrizzlyBears.class, Forest.class})
@DisplayName("Gluttonous Troll")
class GluttonousTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Food token for its opponent when it enters")
    void createsFoodTokenForEachOpponent() {
        castTroll();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifices another nonland permanent to get +2/+2 until end of turn")
    void sacrificesAnotherNonlandPermanentAndBoosts() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new GluttonousTroll());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getEffectivePower()).isEqualTo(5);
        assertThat(troll.getEffectiveToughness()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice a land or the Troll itself")
    void cannotSacrificeLandOrSource() {
        harness.addToBattlefield(player1, new GluttonousTroll());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTroll() {
        harness.castFromHand(player1, new GluttonousTroll(), "{2}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Food can be sacrificed for three life on the turn it is created")
    void foodCanGainLifeImmediately() {
        castTroll();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tapped Food cannot pay its tap activation cost")
    void tappedFoodCannotGainLife() {
        castTroll();
        findPermanent(player1, "Food").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The Troll can sacrifice tapped Food without gaining life")
    void sacrificesTappedFoodToBoost() {
        castTroll();
        Permanent troll = findPermanent(player1, "Gluttonous Troll");
        findPermanent(player1, "Food").tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        assertThat(troll.getEffectivePower()).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(troll.getEffectivePower()).isEqualTo(5);
        assertThat(troll.getEffectiveToughness()).isEqualTo(5);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's permanent to pay the cost")
    void cannotSacrificeOpponentsPermanent() {
        harness.addToBattlefield(player1, new GluttonousTroll());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Repeated boosts accumulate and expire at cleanup")
    void repeatedBoostsExpireAtCleanup() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new GluttonousTroll());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getEffectivePower()).isEqualTo(7);
        assertThat(troll.getEffectiveToughness()).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(troll.getEffectivePower()).isEqualTo(3);
        assertThat(troll.getEffectiveToughness()).isEqualTo(3);
    }
}
