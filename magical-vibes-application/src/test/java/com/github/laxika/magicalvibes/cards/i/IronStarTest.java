package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.b.BogardanFirefiend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IronStarTest extends BaseCardTest {

    // ===== Controller casts red spell =====

    @Test
    @DisplayName("Controller casts red spell, pays {1}, gains 1 life")
    void controllerCastsRedSpellAndPays() {
        harness.addToBattlefield(player1, new IronStar());
        harness.setHand(player1, List.of(new BogardanFirefiend()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        // Trigger goes on the stack unconditionally
        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Iron Star"));

        // Resolving the trigger prompts the may-pay choice
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        // Accept and pay {1}
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts red spell, declines to pay, no life gain")
    void controllerCastsRedSpellAndDeclines() {
        harness.addToBattlefield(player1, new IronStar());
        harness.setHand(player1, List.of(new BogardanFirefiend()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Accepting without enough mana gains no life")
    void acceptWithoutManaNoLife() {
        harness.addToBattlefield(player1, new IronStar());
        harness.setHand(player1, List.of(new BogardanFirefiend()));
        harness.addMana(player1, ManaColor.RED, 3);
        // No spare mana to pay {1}

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Opponent casts red spell =====

    @Test
    @DisplayName("Opponent casts red spell, controller pays {1}, gains 1 life")
    void opponentCastsRedSpellControllerPays() {
        harness.addToBattlefield(player1, new IronStar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BogardanFirefiend()));
        harness.addMana(player2, ManaColor.RED, 3);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);

        // Resolve the trigger (controller of Iron Star chooses)
        harness.passBothPriorities();
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    // ===== Paying by tapping a land during the prompt (CR 605.3a) =====

    @Test
    @DisplayName("Controller taps a land for mana while the pay prompt is open, then pays")
    void tapLandDuringPromptThenPay() {
        harness.addToBattlefield(player1, new IronStar());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new BogardanFirefiend()));
        harness.addMana(player1, ManaColor.RED, 3);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        // CR 605.3a: mana abilities may be activated while an effect asks for a mana payment.
        // Call the service directly — harness.tapPermanent force-clears the awaiting prompt.
        harness.getGameService().tapPermanent(gd, player1, 1); // the Mountain
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Non-choosing player still can't tap lands during the prompt")
    void opponentCannotTapDuringPrompt() {
        harness.addToBattlefield(player1, new IronStar());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new BogardanFirefiend()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.getGameService().tapPermanent(harness.getGameData(), player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("awaiting input");
    }

    // ===== Non-red spell does NOT trigger =====

    @Test
    @DisplayName("Non-red spell does not trigger Iron Star")
    void nonRedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new IronStar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Iron Star"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
