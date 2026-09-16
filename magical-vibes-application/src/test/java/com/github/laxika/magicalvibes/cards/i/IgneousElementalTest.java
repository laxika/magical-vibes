package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IgneousElemental.class, Mountain.class, AirElemental.class})
class IgneousElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {2} less when a land card is in the controller's graveyard")
    void costIsReducedWithLandInGraveyard() {
        harness.setGraveyard(player1, List.of(new Mountain()));
        harness.setHand(player1, List.of(new IgneousElemental()));
        addReducedMana();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the reduced cost without a land card in the graveyard")
    void costIsNotReducedWithoutLandInGraveyard() {
        harness.setHand(player1, List.of(new IgneousElemental()));
        addReducedMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("ETB may deal 2 damage to a target creature")
    void etbMayDealTwoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castWithFullCost();

        selectEtbTarget(target);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the ETB ability deals no damage")
    void decliningEtbDealsNoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castWithFullCost();

        selectEtbTarget(target);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
    }

    private void addReducedMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castWithFullCost() {
        harness.setHand(player1, List.of(new IgneousElemental()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void selectEtbTarget(Permanent target) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
