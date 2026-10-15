package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurybladeVampire.class, Mountain.class})
class FurybladeVampireTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private Permanent addVampire() {
        return harness.addToBattlefieldAndReturn(player1, new FurybladeVampire());
    }

    @Test
    @DisplayName("Accepting may and discarding gives +3/+0")
    void acceptMayDiscardGivesBoost() {
        Permanent vampire = addVampire();
        harness.setHand(player1, List.of(new Mountain()));

        advanceToCombat(player1);
        harness.passBothPriorities(); // Resolve the trigger to open the optional discard prompt.

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(vampire.getPowerModifier()).isEqualTo(3);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Declining may does not discard or boost")
    void declineMayNoBoost() {
        Permanent vampire = addVampire();
        harness.setHand(player1, List.of(new Mountain()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vampire.getPowerModifier()).isEqualTo(0);
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Accepting may with empty hand does not boost")
    void acceptMayEmptyHandNoBoost() {
        Permanent vampire = addVampire();
        harness.setHand(player1, List.of());

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vampire.getPowerModifier()).isEqualTo(0);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent vampire = addVampire();
        harness.setHand(player1, List.of(new Mountain()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(vampire.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(vampire.getPowerModifier()).isEqualTo(0);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        Permanent vampire = addVampire();
        harness.setHand(player1, List.of(new Mountain()));

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(vampire.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each Vampire requires its own discard and boosts only itself")
    void multipleVampiresResolveIndependently() {
        Permanent first = addVampire();
        Permanent second = addVampire();
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(first.getPowerModifier() + second.getPowerModifier()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(first.getPowerModifier()).isEqualTo(3);
        assertThat(second.getPowerModifier()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The discard remains optional and possible after the source leaves")
    void canDiscardAfterSourceLeavesBattlefield() {
        Permanent vampire = addVampire();
        harness.setHand(player1, List.of(new Mountain()));
        advanceToCombat(player1);

        gd.playerBattlefields.get(player1.getId()).remove(vampire);
        gd.playerGraveyards.get(player1.getId()).add(vampire.getCard());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
