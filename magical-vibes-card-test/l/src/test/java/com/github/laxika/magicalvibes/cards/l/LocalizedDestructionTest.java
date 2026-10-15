package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LocalizedDestruction.class, FireElemental.class, GrizzlyBears.class})
class LocalizedDestructionTest extends BaseCardTest {

    @Test
    void paidEnergyProtectsOwnCreaturesWithMatchingPowerBeforeTheWipe() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unmatchedCreature = harness.addToBattlefieldAndReturn(player1, new FireElemental());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        cast();

        PendingInteraction.XValueChoice choice = gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice.maxValue()).isEqualTo(3);
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(unmatchedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void decliningPaymentDestroysAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast();

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }

    @Test
    void grantedIndestructibleWearsOffAtEndOfTurn() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        cast();
        harness.handleXValueChosen(player1, 2);

        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void newlyGainedEnergyCanBePaidImmediately() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setPowerModifier(-1);
        cast();

        harness.handleXValueChosen(player1, 1);

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void protectionUsesModifiedPowerRatherThanPrintedPower() {
        Permanent modifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modifiedCreature.setPowerModifier(1);
        Permanent unmodifiedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        cast();

        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(modifiedCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(unmodifiedCreature);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void decliningPaymentDoesNotProtectZeroPowerCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setPowerModifier(-2);
        cast();

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void paymentIsAllowedEvenWhenNoCreatureHasMatchingPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast();

        harness.handleXValueChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void protectionDoesNotReevaluatePowerOrIncludeCreaturesEnteringLater() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        cast();
        harness.handleXValueChosen(player1, 2);

        protectedCreature.setPowerModifier(1);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new LocalizedDestruction()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
