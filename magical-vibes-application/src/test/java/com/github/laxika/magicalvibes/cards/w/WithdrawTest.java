package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
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

@CardUsed({Withdraw.class, DivingGriffin.class})
class WithdrawTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the first creature and returns the second when its controller declines")
    void returnsBothCreaturesWhenSecondControllerDeclines() {
        Permanent first = addCreatureReady(player2, new DivingGriffin());
        Permanent second = addCreatureReady(player2, new DivingGriffin());
        castWithdraw(first, second);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Diving Griffin");
        assertThat(gd.playerHands.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Diving Griffin"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns the first creature and keeps the second when its controller pays")
    void keepsSecondCreatureWhenItsControllerPays() {
        Permanent first = addCreatureReady(player2, new DivingGriffin());
        Permanent second = addCreatureReady(player2, new DivingGriffin());
        castWithdraw(first, second);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        harness.assertInHand(player2, "Diving Griffin");
    }

    @Test
    @DisplayName("Returns the second creature automatically when its controller cannot pay")
    void returnsSecondCreatureWhenItsControllerCannotPay() {
        Permanent first = addCreatureReady(player2, new DivingGriffin());
        Permanent second = addCreatureReady(player2, new DivingGriffin());
        castWithdraw(first, second);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Diving Griffin");
        assertThat(gd.playerHands.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Diving Griffin"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Asks the second target's controller whether to pay")
    void asksSecondTargetControllerToPay() {
        Permanent first = addCreatureReady(player2, new DivingGriffin());
        Permanent second = addCreatureReady(player1, new DivingGriffin());
        castWithdraw(first, second);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player2, "Diving Griffin");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(second);
        harness.assertNotInHand(player1, "Diving Griffin");
    }

    @Test
    @DisplayName("Resolves the remaining target when the first target leaves")
    void resolvesRemainingTargetWhenFirstTargetLeaves() {
        Permanent first = addCreatureReady(player2, new DivingGriffin());
        Permanent second = addCreatureReady(player2, new DivingGriffin());
        castWithdraw(first, second);
        gd.playerBattlefields.get(player2.getId()).remove(first);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Diving Griffin");
        assertThat(gd.playerHands.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Diving Griffin"))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void cannotChooseSameCreatureTwice() {
        Permanent creature = addCreatureReady(player2, new DivingGriffin());
        harness.setHand(player1, List.of(new Withdraw()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithdraw(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new Withdraw()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
    }
}
