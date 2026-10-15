package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistbreathElder.class, BarkformHarvester.class, Forest.class, VorinclexMonstrousRaider.class})
class MistbreathElderTest extends BaseCardTest {

    @Test
    void returnsAnotherCreatureAndGetsCounter() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(creature.getId());

        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(elder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void withNoOtherCreatureMayReturnItself() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elder);
        assertThat(gd.playerHands.get(player1.getId())).contains(elder.getCard());
    }

    @Test
    void decliningSelfReturnDoesNothing() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elder);
        assertThat(elder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsCreaturesAndOwnLandsDoNotPreventOptionalSelfReturn() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BarkformHarvester());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(elder.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land).doesNotContain(elder);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elder, creature);
        assertThat(elder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void returnsStolenCreatureToItsOwnerAndStillGetsCounter() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        gd.stolenCreatures.put(creature.getId(), player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(elder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void stillReturnsAnotherCreatureWhenElderLeavesBeforeResolution() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());

        advanceToUpkeep(player1);
        harness.getPermanentRemovalService().removePermanentToHand(gd, elder);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(elder.getCard(), creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elder, creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choosesFromCreaturesPresentAtResolutionRatherThanAtTriggerTime() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());

        advanceToUpkeep(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elder).doesNotContain(creature);
        assertThat(elder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterPlacementUsesTriggerControllerAfterElderChangesControl() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new MistbreathElder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(elder);
        gd.playerBattlefields.get(player2.getId()).add(elder);
        gd.stolenCreatures.put(elder.getId(), player1.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elder);
        assertThat(elder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
