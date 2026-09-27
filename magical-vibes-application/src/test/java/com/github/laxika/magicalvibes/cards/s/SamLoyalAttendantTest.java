package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.cards.f.FrodoAdventurousHobbit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamLoyalAttendant.class, FrodoAdventurousHobbit.class, Food.class})
class SamLoyalAttendantTest extends BaseCardTest {

    @Test
    void partnerWithLetsTargetPlayerSearchForFrodo() {
        harness.setLibrary(player2, List.of(new FrodoAdventurousHobbit()));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new SamLoyalAttendant());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement().isInstanceOf(FrodoAdventurousHobbit.class);
    }

    @Test
    void createsFoodAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new SamLoyalAttendant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void reducesFoodActivationCostForFoodsYouControl() {
        harness.addToBattlefield(player1, new SamLoyalAttendant());
        harness.addToBattlefield(player1, new Food());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceFoodActivationCostForOpponent() {
        harness.addToBattlefield(player1, new SamLoyalAttendant());
        harness.addToBattlefield(player2, new Food());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
