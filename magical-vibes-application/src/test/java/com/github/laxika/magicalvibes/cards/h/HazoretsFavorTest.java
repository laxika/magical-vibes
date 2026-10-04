package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HazoretsFavor.class, Colossapede.class})
class HazoretsFavorTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Accepting grants +2/+0 and haste to the target creature")
    void acceptGrantsBoostAndHaste() {
        harness.addToBattlefield(player1, new HazoretsFavor());
        harness.addToBattlefield(player1, new Colossapede());
        UUID bearsId = harness.getPermanentId(player1, "Colossapede");

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Colossapede");
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Accepting sacrifices the target at the beginning of the next end step")
    void acceptSacrificesTargetAtEndStep() {
        harness.addToBattlefield(player1, new HazoretsFavor());
        harness.addToBattlefield(player1, new Colossapede());
        UUID bearsId = harness.getPermanentId(player1, "Colossapede");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Colossapede");

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Colossapede");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Colossapede");
        harness.assertInGraveyard(player1, "Colossapede");
    }

    @Test
    @DisplayName("Declining does not boost or sacrifice the creature")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new HazoretsFavor());
        harness.addToBattlefield(player1, new Colossapede());
        UUID bearsId = harness.getPermanentId(player1, "Colossapede");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent bears = findPermanent(player1, "Colossapede");
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.HASTE);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.assertOnBattlefield(player1, "Colossapede");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new HazoretsFavor());
        harness.addToBattlefield(player1, new Colossapede());
        harness.addToBattlefield(player2, new Colossapede());
        UUID opponentBearsId = harness.getPermanentId(player2, "Colossapede");

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new HazoretsFavor());
        harness.addToBattlefield(player1, new Colossapede());

        advanceToCombat(player2); // opponent's combat
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target the enchantment itself")
    void cannotTargetNoncreature() {
        Permanent favor = harness.addToBattlefieldAndReturn(player1, new HazoretsFavor());
        harness.addToBattlefield(player1, new Colossapede());
        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, favor.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Combat with no creatures leaves no trigger or choice pending")
    void noLegalTargets() {
        harness.addToBattlefield(player1, new HazoretsFavor());
        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A creature taken by an opponent survives the delayed sacrifice")
    void opponentControlledCreatureSurvivesDelayedSacrifice() {
        harness.addToBattlefield(player1, new HazoretsFavor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Model a control change while preserving the same battlefield object.
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Colossapede");
        harness.assertNotInGraveyard(player1, "Colossapede");
        harness.assertNotInGraveyard(player2, "Colossapede");
    }

    @Test
    @DisplayName("The delayed sacrifice retains Hazoret's Favor as its source")
    void delayedTriggerRetainsItsSource() {
        harness.addToBattlefield(player1, new HazoretsFavor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(HazoretsFavor.class);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Colossapede");
    }
}
