package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Facevaulter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmolderInitiateTest extends BaseCardTest {

    /** Puts player2 on the active turn so their black spell isn't cast on player1's own turn (irrelevant here, but keeps mana clean). */
    private void opponentCastsBlackSpell(Player caster) {
        harness.setHand(caster, List.of(new Facevaulter()));
        harness.addMana(caster, ManaColor.BLACK, 5);
    }

    @Test
    @DisplayName("A player casting a black spell prompts the controller's may-pay ability")
    void blackSpellTriggersMayPay() {
        harness.addToBattlefield(player1, new SmolderInitiate());
        opponentCastsBlackSpell(player1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Paying {1} makes the chosen target player lose 1 life")
    void payMakesTargetLoseLife() {
        harness.addToBattlefield(player1, new SmolderInitiate());

        // Opponent casts the black spell so player1's payment mana stays isolated.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        opponentCastsBlackSpell(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("The controller may target themselves")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new SmolderInitiate());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        opponentCastsBlackSpell(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Declining leaves life totals unchanged")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new SmolderInitiate());
        opponentCastsBlackSpell(player1);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        while (!harness.getGameData().stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting a non-black spell does not trigger the ability")
    void nonBlackSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SmolderInitiate());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }
}
