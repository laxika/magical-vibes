package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UginTheIneffable;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GideonsSacrifice.class, HillGiant.class, Shock.class, UginTheIneffable.class})
class GideonsSacrificeTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects damage to the chosen creature instead of the controller")
    void redirectsDamageToChosenCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        castAndChoose(chosen);
        castShockAtPlayer(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(chosen.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Redirects damage to the chosen creature instead of another permanent")
    void redirectsDamageToChosenCreatureFromControlledPermanent() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        castAndChoose(chosen);
        castShockAtPermanent(other);

        assertThat(other.getMarkedDamage()).isZero();
        assertThat(chosen.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can redirect damage to a chosen planeswalker")
    void redirectsDamageToChosenPlaneswalker() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new UginTheIneffable());
        int startingLoyalty = chosen.getCounterCount(CounterType.LOYALTY);

        castAndChoose(chosen);
        castShockAtPlayer(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(chosen.getCounterCount(CounterType.LOYALTY)).isEqualTo(startingLoyalty - 2);
    }

    private void castAndChoose(Permanent chosen) {
        harness.setHand(player1, List.of(new GideonsSacrifice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
    }

    private void castShockAtPlayer(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player.getId());
        harness.passBothPriorities();
    }

    private void castShockAtPermanent(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
