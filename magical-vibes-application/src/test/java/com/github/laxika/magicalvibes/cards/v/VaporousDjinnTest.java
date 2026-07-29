package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VaporousDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {U}{U} phases the Djinn out")
    void declinePhasesOut() {
        Permanent djinn = addDjinn();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(djinn);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(djinn);
    }

    @Test
    @DisplayName("Paying {U}{U} keeps the Djinn on the battlefield and spends the mana")
    void payKeepsItOnBattlefield() {
        Permanent djinn = addDjinn();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 2); // mana empties between steps — add it at payment time
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(djinn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough blue mana still phases the Djinn out")
    void acceptWithoutManaPhasesOut() {
        Permanent djinn = addDjinn();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1); // one short
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(djinn);
    }

    @Test
    @DisplayName("A phased-out Djinn phases in during its controller's next untap step")
    void phasesBackIn() {
        Permanent djinn = addDjinn();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(djinn);

        advanceTurn(); // player2's turn — still phased out
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(djinn);

        advanceTurn(); // player1's untap step — the Djinn phases in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(djinn);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent djinn = addDjinn();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(djinn);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addDjinn() {
        Permanent perm = new Permanent(new VaporousDjinn());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}
