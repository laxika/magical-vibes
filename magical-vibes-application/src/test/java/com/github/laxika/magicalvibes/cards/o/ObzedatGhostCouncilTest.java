package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObzedatGhostCouncil.class})
class ObzedatGhostCouncilTest extends BaseCardTest {

    @Test
    @DisplayName("ETB drains the target opponent for 2 and gains 2 life")
    void etbDrainsTargetOpponent() {
        castObzedat();
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Cannot target yourself with the ETB drain")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new ObzedatGhostCouncil()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Accepting the end step trigger exiles it")
    void endStepExilesWhenAccepted() {
        harness.addToBattlefield(player1, new ObzedatGhostCouncil());

        exileAtEndStep(true);

        assertThat(findObzedat(player1)).isNull();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Obzedat, Ghost Council"));
    }

    @Test
    @DisplayName("Declining the end step trigger leaves it on the battlefield")
    void endStepKeepsItWhenDeclined() {
        harness.addToBattlefield(player1, new ObzedatGhostCouncil());

        exileAtEndStep(false);

        assertThat(findObzedat(player1)).isNotNull();
    }

    @Test
    @DisplayName("Stays exiled through an opponent's upkeep, returns with haste at its controller's upkeep")
    void returnsAtControllersNextUpkeepWithHaste() {
        harness.addToBattlefield(player1, new ObzedatGhostCouncil());
        exileAtEndStep(true);

        runUpkeepOf(player2);
        assertThat(findObzedat(player1)).isNull();

        runUpkeepOf(player1);
        assertThat(findObzedat(player1)).isNull();
        harness.passBothPriorities(); // resolve the delayed return trigger

        Permanent returned = findObzedat(player1);
        assertThat(returned).isNotNull();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Returning from exile re-triggers the ETB drain")
    void returnRetriggersEtbDrain() {
        harness.addToBattlefield(player1, new ObzedatGhostCouncil());
        exileAtEndStep(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        runUpkeepOf(player1);
        harness.passBothPriorities(); // resolve the delayed return trigger
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("A borrowed Obzedat waits for its ability controller's upkeep, then returns to its owner")
    void borrowedObzedatReturnsAtAbilityControllersUpkeep() {
        ObzedatGhostCouncil card = new ObzedatGhostCouncil();
        card.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, card);
        exileAtEndStep(true);

        runUpkeepOf(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findObzedat(player1)).isNull();
        Permanent returned = findObzedat(player2);
        assertThat(returned).isNotNull();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("A borrowed Obzedat does not return during its owner's intervening upkeep")
    void borrowedObzedatSkipsOwnersUpkeep() {
        ObzedatGhostCouncil card = new ObzedatGhostCouncil();
        card.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, card);
        exileAtEndStep(true);

        runUpkeepOf(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findObzedat(player1)).isNull();
        assertThat(findObzedat(player2)).isNull();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(card.getId()));
    }

    @Test
    @DisplayName("Obzedat does not trigger during an opponent's end step")
    void opponentsEndStepDoesNotTrigger() {
        harness.addToBattlefield(player1, new ObzedatGhostCouncil());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Obzedat, Ghost Council");
    }

    private void exileAtEndStep(boolean accept) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger goes on the stack
        harness.passBothPriorities(); // resolve the trigger → may prompt
        harness.handleMayAbilityChosen(player1, accept);
        harness.clearPriorityPassed();
    }

    private void runUpkeepOf(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance into the upkeep, firing its delayed returns
    }

    private Permanent findObzedat(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Obzedat, Ghost Council"))
                .findFirst()
                .orElse(null);
    }

    private void castObzedat() {
        harness.setHand(player1, List.of(new ObzedatGhostCouncil()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, player2.getId());
    }
}
