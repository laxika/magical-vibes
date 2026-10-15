package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScaldingSalamander.class, SabertoothWyvern.class, JaceBeleren.class})
class ScaldingSalamanderTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the attack trigger damages defending creatures without flying")
    void damagesDefendingCreaturesWithoutFlying() {
        addCreatureReady(player1, new ScaldingSalamander());
        Permanent defendingSalamander = addCreatureReady(player2, new ScaldingSalamander());
        Permanent defendingSalamander2 = addCreatureReady(player2, new ScaldingSalamander());
        Permanent defendingWyvern = addCreatureReady(player2, new SabertoothWyvern());
        Permanent ownSalamander = addCreatureReady(player1, new ScaldingSalamander());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(defendingSalamander.getMarkedDamage()).isEqualTo(1);
        assertThat(defendingSalamander2.getMarkedDamage()).isEqualTo(1);
        assertThat(defendingWyvern.getMarkedDamage()).isZero();
        assertThat(ownSalamander.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Accepting the attack trigger with no matching creatures deals no damage")
    void acceptingAttackTriggerWithNoMatchingCreaturesDealsNoDamage() {
        addCreatureReady(player1, new ScaldingSalamander());
        Permanent defendingWyvern = addCreatureReady(player2, new SabertoothWyvern());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(defendingWyvern.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the attack trigger deals no damage")
    void decliningAttackTriggerDealsNoDamage() {
        addCreatureReady(player1, new ScaldingSalamander());
        Permanent defendingSalamander = addCreatureReady(player2, new ScaldingSalamander());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(defendingSalamander.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The attack trigger still deals damage after its source leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new ScaldingSalamander());
        Permanent defender = addCreatureReady(player2, new ScaldingSalamander());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Scalding Salamander");
    }

    @Test
    @DisplayName("Removing the attacked planeswalker does not erase the defending player")
    void triggerResolvesAfterAttackedPlaneswalkerLeavesBattlefield() {
        addCreatureReady(player1, new ScaldingSalamander());
        Permanent defender = addCreatureReady(player2, new ScaldingSalamander());
        Permanent planeswalker = new Permanent(new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Scalding Salamander");
    }
}
