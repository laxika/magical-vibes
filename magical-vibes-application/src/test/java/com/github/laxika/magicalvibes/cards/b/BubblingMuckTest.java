package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FalseDawn;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BubblingMuck.class, Mountain.class, Swamp.class, FalseDawn.class})
class BubblingMuckTest extends BaseCardTest {

    @Test
    @DisplayName("A Swamp tapped for mana adds an additional {B}")
    void addsBlackManaWhenSwampIsTapped() {
        harness.addToBattlefield(player1, new Swamp());

        harness.castFromHand(player1, new BubblingMuck(), "{B}");
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("The effect applies to an opponent's Swamp and not to a Mountain")
    void appliesSymmetricallyOnlyToSwamps() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Swamp());

        harness.castFromHand(player1, new BubblingMuck(), "{B}");
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("The additional mana effect expires at the end of the turn")
    void expiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new Swamp());

        harness.castFromHand(player1, new BubblingMuck(), "{B}");
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        Permanent swamp = findPermanent(player1, "Swamp");
        swamp.untap();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("The effect remains active during the end step")
    void remainsActiveDuringEndStep() {
        harness.addToBattlefield(player1, new Swamp());

        harness.castFromHand(player1, new BubblingMuck(), "{B}");
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple resolved copies each add one black mana immediately")
    void multipleCopiesStack() {
        harness.addToBattlefield(player1, new Swamp());

        harness.castFromHand(player1, new BubblingMuck(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new BubblingMuck(), "{B}");
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Swamps entering after resolution also add the additional mana")
    void appliesToSwampEnteringAfterResolution() {
        harness.castFromHand(player1, new BubblingMuck(), "{B}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Swamp());
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("False Dawn replaces the caster's Bubbling Muck mana with white mana")
    void falseDawnReplacesAdditionalMana() {
        harness.addToBattlefield(player1, new Swamp());

        harness.castFromHand(player1, new BubblingMuck(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new FalseDawn(), "{1}{W}");
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("False Dawn replaces the caster's delayed trigger even when an opponent taps a Swamp")
    void falseDawnReplacesAdditionalManaGivenToOpponent() {
        harness.addToBattlefield(player2, new Swamp());

        harness.castFromHand(player1, new BubblingMuck(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new FalseDawn(), "{1}{W}");
        harness.passBothPriorities();
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
