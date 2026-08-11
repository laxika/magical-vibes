package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaryFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at your end step after another creature entered under your control")
    void surveilsAfterAnotherCreatureEnteredUnderYourControl() {
        WaryFarmer farmer = new WaryFarmer();
        harness.addToBattlefield(player1, farmer);
        Card enteredCreature = new GrizzlyBears();
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(enteredCreature);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Does not trigger when only Wary Farmer entered under your control")
    void doesNotTriggerForItsOwnEntry() {
        WaryFarmer farmer = new WaryFarmer();
        harness.addToBattlefield(player1, farmer);
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(farmer);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a creature that entered under an opponent's control")
    void doesNotTriggerForOpponentCreatureEntry() {
        harness.addToBattlefield(player1, new WaryFarmer());
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player2.getId(), ignored -> new ArrayList<>())
                .add(new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
