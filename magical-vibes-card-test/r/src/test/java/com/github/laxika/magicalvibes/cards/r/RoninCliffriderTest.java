package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HiredMuscle;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Scarmaker;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoninCliffrider.class, HiredMuscle.class, Scarmaker.class, JaceBeleren.class})
class RoninCliffriderTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the attack trigger deals 1 damage to each defending creature")
    void attackTriggerDamagesDefendingCreatures() {
        addReadyRonin(player1);
        Permanent defendingMuscle = addCreatureReady(player2, new HiredMuscle());
        Permanent ownMuscle = addCreatureReady(player1, new HiredMuscle());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(defendingMuscle.getMarkedDamage()).isEqualTo(1);
        assertThat(ownMuscle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the attack trigger deals no damage")
    void decliningAttackTriggerDealsNoDamage() {
        addReadyRonin(player1);
        Permanent defendingMuscle = addCreatureReady(player2, new HiredMuscle());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(defendingMuscle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Bushido gives Ronin Cliffrider +1/+1 when it blocks")
    void bushidoWhenBlocking() {
        addCreatureReady(player1, new HiredMuscle());
        Permanent ronin = addReadyRonin(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido gives Ronin Cliffrider +1/+1 when it becomes blocked")
    void bushidoWhenBecomesBlocked() {
        Permanent ronin = addReadyRonin(player1);
        addCreatureReady(player2, new HiredMuscle());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido's bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new HiredMuscle());
        Permanent ronin = addReadyRonin(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isZero();
        assertThat(ronin.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack trigger damages every defending creature even after Ronin leaves")
    void attackTriggerResolvesAfterSourceLeaves() {
        Permanent ronin = addReadyRonin(player1);
        Permanent firstDefender = addCreatureReady(player2, new HiredMuscle());
        Permanent secondDefender = addCreatureReady(player2, new HiredMuscle());
        Permanent ownCreature = addCreatureReady(player1, new HiredMuscle());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(ronin);
        gd.playerGraveyards.get(player1.getId()).add(ronin.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(firstDefender.getMarkedDamage()).isEqualTo(1);
        assertThat(secondDefender.getMarkedDamage()).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Becoming blocked by two creatures triggers bushido only once")
    void multipleBlockersTriggerBushidoOnce() {
        Permanent ronin = addReadyRonin(player1);
        addCreatureReady(player2, new HiredMuscle());
        addCreatureReady(player2, new HiredMuscle());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("The attack trigger still damages defending creatures after the attacked planeswalker leaves")
    void attackTriggerResolvesAfterAttackedPlaneswalkerLeaves() {
        addReadyRonin(player1);
        Permanent defender = addCreatureReady(player2, new HiredMuscle());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addReadyRonin(Player player) {
        return addCreatureReady(player, new RoninCliffrider());
    }
}
