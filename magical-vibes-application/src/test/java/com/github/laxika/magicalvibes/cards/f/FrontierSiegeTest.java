package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FrontierSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Khans adds two green mana at both of the controller's main phases")
    void khansAddsManaAtBothMainPhases() {
        castSiege(player1, "Khans");

        advanceToPrecombatMain(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);

        advanceToPostcombatMain(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Khans does not add mana during an opponent's main phase")
    void khansDoesNotAddManaDuringOpponentsMainPhase() {
        castSiege(player1, "Khans");

        advanceToPrecombatMain(player2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Dragons may have an entering flying creature fight an opponent creature")
    void dragonsFlyingCreatureFightsOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSiege(player1, "Dragons");

        castFlyingCreature(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opponentBears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentBears.getId()));
    }

    @Test
    @DisplayName("Dragons does not trigger for a nonflying creature")
    void dragonsDoesNotTriggerForNonflyingCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSiege(player1, "Dragons");

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentBears.getMarkedDamage()).isZero();
    }

    private void castSiege(Player player, String mode) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new FrontierSiege()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player, mode);
    }

    private void castFlyingCreature(Player player) {
        harness.setHand(player, List.of(new FaerieMiscreant()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.castCreature(player, 0);
    }

    private void advanceToPrecombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
