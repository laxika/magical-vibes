package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhysticStudy.class, PygmyRazorback.class, RhysticDeluge.class})
class RhysticStudyTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when an opponent casts any spell")
    void triggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new RhysticStudy());
        prepareOpponentTurn();

        harness.castFromHand(player2, new PygmyRazorback(), "{1}{G}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Does not trigger when the controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new RhysticStudy());
        harness.castFromHand(player1, new RhysticDeluge(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Opponent pays one mana to prevent the draw")
    void opponentPaysToPreventDraw() {
        castOpponentSpellWithManaToPay();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("If the opponent declines, the controller may draw")
    void controllerMayDrawWhenOpponentDeclines() {
        castOpponentSpellWithManaToPay();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("If the opponent cannot pay, the controller may draw")
    void controllerMayDrawWhenOpponentCannotPay() {
        castOpponentSpellWithoutManaToPay();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The controller may decline the draw after the opponent declines to pay")
    void controllerMayDeclineDraw() {
        castOpponentSpellWithManaToPay();
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void castOpponentSpellWithManaToPay() {
        harness.addToBattlefield(player1, new RhysticStudy());
        prepareOpponentTurn();

        harness.castFromHand(player2, new RhysticDeluge(), "{2}{U}");
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    private void castOpponentSpellWithoutManaToPay() {
        harness.addToBattlefield(player1, new RhysticStudy());
        prepareOpponentTurn();

        harness.castFromHand(player2, new PygmyRazorback(), "{1}{G}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
