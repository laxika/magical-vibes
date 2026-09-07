package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolcanoHellion.class, AirElemental.class})
class VolcanoHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the chosen damage to you and the target creature")
    void dealsChosenDamageToYouAndTargetCreature() {
        Permanent target = castVolcanoHellion();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The damage cannot be prevented")
    void damageCannotBePrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        gd.playerDamagePreventionShields.put(player1.getId(), 10);
        target.setDamagePreventionShield(10);
        castVolcanoHellion(target);

        harness.handleXValueChosen(player1, 3);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Allows choosing zero damage")
    void allowsChoosingZeroDamage() {
        Permanent target = castVolcanoHellion();

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isZero();
    }

    private Permanent castVolcanoHellion() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castVolcanoHellion(target);
        return target;
    }

    private void castVolcanoHellion(Permanent target) {
        harness.setHand(player1, List.of(new VolcanoHellion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
