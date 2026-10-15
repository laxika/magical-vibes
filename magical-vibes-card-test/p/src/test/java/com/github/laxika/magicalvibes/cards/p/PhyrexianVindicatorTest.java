package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianVindicator.class, Shock.class, GrizzlyBears.class, HillGiant.class})
class PhyrexianVindicatorTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents burn damage and deals the prevented amount to another target player")
    void preventsDamageAndDealsItToPlayer() {
        Permanent vindicator = harness.addToBattlefieldAndReturn(player2, new PhyrexianVindicator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, vindicator.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).doesNotContain(vindicator.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(vindicator.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Deals prevented damage to another creature")
    void preventsDamageAndDealsItToCreature() {
        Permanent vindicator = harness.addToBattlefieldAndReturn(player2, new PhyrexianVindicator());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, vindicator.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Phyrexian Vindicator");
    }

    @Test
    @DisplayName("Prevents combat damage and deals it to the attacking player")
    void preventsCombatDamageAndDealsItToPlayer() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent vindicator = harness.addToBattlefieldAndReturn(player2, new PhyrexianVindicator());
        vindicator.setSummoningSick(false);
        vindicator.setBlocking(true);
        vindicator.addBlockingTarget(0);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player2, "Phyrexian Vindicator");
    }

    @Test
    @CardUsed({DressDown.class})
    @DisplayName("Damage is not prevented after Vindicator loses all abilities")
    void doesNotPreventDamageWithoutAbilities() {
        Permanent vindicator = harness.addToBattlefieldAndReturn(player2, new PhyrexianVindicator());
        harness.addToBattlefield(player1, new DressDown());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, vindicator.getId());
        harness.passBothPriorities();

        assertThat(vindicator.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The prevented damage may be dealt to Vindicator's controller")
    void canDealPreventedDamageToItsController() {
        Permanent vindicator = harness.addToBattlefieldAndReturn(player2, new PhyrexianVindicator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, vindicator.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(vindicator.getMarkedDamage()).isZero();
    }
}
