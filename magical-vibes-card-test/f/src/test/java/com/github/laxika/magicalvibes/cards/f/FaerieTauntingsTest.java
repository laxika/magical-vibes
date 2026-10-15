package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieTauntings.class, Tarfire.class})
class FaerieTauntingsTest extends BaseCardTest {

    /** Puts player1 on defense during player2's turn so player1 may cast an instant. */
    private void enterOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Accepting makes each opponent lose 1 life")
    void acceptDrainsOpponent() {
        harness.addToBattlefield(player1, new FaerieTauntings());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        // Tarfire deals 2 to player2, and the trigger drains 1 more.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    @DisplayName("Declining leaves opponent life unchanged by the trigger")
    void declineLeavesLife() {
        harness.addToBattlefield(player1, new FaerieTauntings());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        GameData gd = harness.getGameData();
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        // Only Tarfire's 2 damage, no drain.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Casting on your own turn does not trigger")
    void doesNotTriggerOnOwnTurn() {
        harness.addToBattlefield(player1, new FaerieTauntings());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Opponent casting during their turn does not trigger your enchantment")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new FaerieTauntings());
        enterOpponentTurn();
        harness.setHand(player2, List.of(new Tarfire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each spell cast during the opponent's turn triggers separately")
    void triggersForEverySpell() {
        harness.addToBattlefield(player1, new FaerieTauntings());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire(), new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("The trigger drains opponents even when the spell targets its own caster")
    void spellTargetDoesNotDetermineLifeLossRecipient() {
        harness.addToBattlefield(player1, new FaerieTauntings());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
    }
}
