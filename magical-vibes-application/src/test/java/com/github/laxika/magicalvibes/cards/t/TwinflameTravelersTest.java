package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CeaselessSearblades;
import com.github.laxika.magicalvibes.cards.f.FireBellyChangeling;
import com.github.laxika.magicalvibes.cards.f.FlamekinHarbinger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwinflameTravelers.class, FlamekinHarbinger.class, MentorOfTheMeek.class,
        GrizzlyBears.class, CeaselessSearblades.class, FireBellyChangeling.class})
class TwinflameTravelersTest extends BaseCardTest {

    @Test
    @DisplayName("Another Elemental's triggered ability triggers twice")
    void doublesAnotherElementalsTriggeredAbility() {
        harness.addToBattlefield(player1, new TwinflameTravelers());

        harness.castFromHand(player1, new FlamekinHarbinger(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("A non-Elemental's triggered ability triggers only once")
    void doesNotDoubleNonElementalTriggeredAbility() {
        harness.addToBattlefield(player1, new TwinflameTravelers());
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Travelers adds one trigger rather than doubling the total")
    void multipleTravelersAddTriggers() {
        harness.addToBattlefield(player1, new TwinflameTravelers());
        harness.addToBattlefield(player1, new TwinflameTravelers());

        harness.castFromHand(player1, new FlamekinHarbinger(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
    }

    @Test
    @DisplayName("Travelers does not multiply an opposing Elemental's trigger")
    void doesNotDoubleOpponentsElementalTrigger() {
        harness.addToBattlefield(player2, new TwinflameTravelers());

        harness.castFromHand(player1, new FlamekinHarbinger(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each additional optional trigger resolves independently")
    void optionalTriggersResolveIndependently() {
        harness.addToBattlefield(player1, new TwinflameTravelers());
        harness.castFromHand(player1, new FlamekinHarbinger(), "{R}");
        harness.passBothPriorities();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An Elemental's ability-activation trigger also triggers an additional time")
    void doublesElementalAbilityActivationTrigger() {
        harness.addToBattlefield(player1, new TwinflameTravelers());
        Permanent searblades = addCreatureReady(player1, new CeaselessSearblades());
        addCreatureReady(player1, new FireBellyChangeling());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        assertThat(searblades.getPowerModifier()).isEqualTo(2);
        assertThat(searblades.getToughnessModifier()).isZero();
    }
}
