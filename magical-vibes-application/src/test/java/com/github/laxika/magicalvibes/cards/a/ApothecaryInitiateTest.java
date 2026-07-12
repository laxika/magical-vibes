package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApothecaryInitiateTest extends BaseCardTest {

    // ===== Controller casts a white spell =====

    @Test
    @DisplayName("Controller casts white spell, pays {1}, gains 1 life")
    void controllerCastsWhiteSpellAndPays() {
        harness.addToBattlefield(player1, new ApothecaryInitiate());
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1); // Elite Vanguard's {W}
        harness.addMana(player1, ManaColor.COLORLESS, 1); // the {1} to pay

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Apothecary Initiate"));

        harness.passBothPriorities(); // resolve triggered ability

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts white spell, declines, no life gain")
    void controllerCastsWhiteSpellAndDeclines() {
        harness.addToBattlefield(player1, new ApothecaryInitiate());
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Apothecary Initiate"));

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Opponent casts a white spell =====

    @Test
    @DisplayName("Opponent casts white spell, controller pays {1}, gains 1 life")
    void opponentCastsWhiteSpellControllerPays() {
        harness.addToBattlefield(player1, new ApothecaryInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 1); // controller's {1} to pay

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new EliteVanguard()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    // ===== Non-white spell does not trigger =====

    @Test
    @DisplayName("Non-white spell does not trigger Apothecary Initiate")
    void nonWhiteSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ApothecaryInitiate());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
