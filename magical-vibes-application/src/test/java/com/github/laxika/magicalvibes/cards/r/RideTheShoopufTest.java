package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RideTheShoopuf.class, Forest.class, GrizzlyBears.class})
class RideTheShoopufTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on target creature you control")
    void landfallPutsCounterOnTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new RideTheShoopuf());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall cannot target a creature controlled by an opponent")
    void landfallCannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new RideTheShoopuf());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability permanently makes Ride the Shoopuf a 7/7 Beast creature")
    void activatedAbilityPermanentlyAnimatesRideTheShoopuf() {
        Permanent shoopuf = harness.addToBattlefieldAndReturn(player1, new RideTheShoopuf());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, shoopuf)).isTrue();
        assertThat(gqs.isEnchantment(gd, shoopuf)).isTrue();
        assertThat(gqs.getEffectivePower(gd, shoopuf)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, shoopuf)).isEqualTo(7);
        assertThat(shoopuf.getGrantedSubtypes()).contains(CardSubtype.BEAST);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, shoopuf)).isTrue();
    }
}
