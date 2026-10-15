package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MesaEnchantress.class, RuneclawBear.class, HonorOfThePure.class})
class MesaEnchantressTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an enchantment puts the trigger on the stack before the draw choice")
    void enchantmentCastTriggersMayPrompt() {
        harness.addToBattlefield(player1, new MesaEnchantress());
        harness.castFromHand(player1, new HonorOfThePure(), "{1}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player1, "Honor of the Pure");
    }

    @Test
    @DisplayName("Accepting at resolution draws exactly one card before the enchantment resolves")
    void acceptDrawsACard() {
        harness.addToBattlefield(player1, new MesaEnchantress());
        RuneclawBear drawnCard = new RuneclawBear();
        RuneclawBear remainingCard = new RuneclawBear();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));
        harness.castFromHand(player1, new HonorOfThePure(), "{1}{W}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Honor of the Pure");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Honor of the Pure");
    }

    @Test
    @DisplayName("Declining at resolution leaves the library unchanged")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new MesaEnchantress());
        RuneclawBear libraryCard = new RuneclawBear();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new HonorOfThePure(), "{1}{W}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertNotInHand(player1, "Runeclaw Bear");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Honor of the Pure");
    }

    @Test
    @DisplayName("Non-enchantment spell does not trigger Mesa Enchantress")
    void nonEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new MesaEnchantress());
        harness.castFromHand(player1, new RuneclawBear(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting enchantment does not trigger Mesa Enchantress")
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new MesaEnchantress());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new HonorOfThePure(), "{1}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("An enchantment entering without being cast does not trigger")
    void enchantmentEnteringWithoutBeingCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new MesaEnchantress());
        harness.enterBattlefieldAndReturn(player1, new HonorOfThePure());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Honor of the Pure");
    }

    @Test
    @DisplayName("Casting Mesa Enchantress itself does not trigger its ability")
    void castingMesaEnchantressDoesNotTrigger() {
        harness.castFromHand(player1, new MesaEnchantress(), "{1}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Mesa Enchantress");
    }
}