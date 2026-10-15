package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SlipperyBogle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarrenTorchmaster.class, GrizzlyBears.class, SlipperyBogle.class})
class WarrenTorchmasterTest extends BaseCardTest {

    @Test
    void blightsOneCreatureThenTargetsADifferentCreatureForHaste() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent torchmaster = harness.addToBattlefieldAndReturn(player1, new WarrenTorchmaster());
        torchmaster.setSummoningSick(false);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, torchmaster.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(torchmaster.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(torchmaster.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void decliningBlightDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent torchmaster = harness.addToBattlefieldAndReturn(player1, new WarrenTorchmaster());
        torchmaster.setSummoningSick(false);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(torchmaster.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(torchmaster.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void canBlightAndGrantHasteToItself() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent torchmaster = harness.addToBattlefieldAndReturn(player1, new WarrenTorchmaster());

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(torchmaster.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(torchmaster.hasKeyword(Keyword.HASTE)).isFalse();
        harness.handlePermanentChosen(player1, torchmaster.getId());
        assertThat(torchmaster.hasKeyword(Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        assertThat(torchmaster.hasKeyword(Keyword.HASTE)).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(torchmaster.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(torchmaster.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void canGrantHasteToAnOpponentsCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent torchmaster = harness.addToBattlefieldAndReturn(player1, new WarrenTorchmaster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WarrenTorchmaster());

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(torchmaster.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent torchmaster = harness.addToBattlefieldAndReturn(player1, new WarrenTorchmaster());

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(torchmaster.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void hasteTargetChoiceExcludesOpponentsHexproofCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent torchmaster = harness.addToBattlefieldAndReturn(player1, new WarrenTorchmaster());
        Permanent bogle = harness.addToBattlefieldAndReturn(player2, new SlipperyBogle());

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(torchmaster.getId()).doesNotContain(bogle.getId());
    }

    @Test
    void canGrantHasteToOwnHexproofCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent torchmaster = harness.addToBattlefieldAndReturn(player1, new WarrenTorchmaster());
        Permanent bogle = harness.addToBattlefieldAndReturn(player1, new SlipperyBogle());

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, torchmaster.getId());
        harness.handlePermanentChosen(player1, bogle.getId());
        harness.passBothPriorities();

        assertThat(torchmaster.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(bogle.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(bogle.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void creatureKilledByBlightIsNotOfferedAsAHasteTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent torchmaster = harness.addToBattlefieldAndReturn(player1, new WarrenTorchmaster());
        Permanent bogle = harness.addToBattlefieldAndReturn(player1, new SlipperyBogle());

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bogle.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bogle);
        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(torchmaster.getId()).doesNotContain(bogle.getId());
        harness.handlePermanentChosen(player1, torchmaster.getId());
        harness.passBothPriorities();

        assertThat(torchmaster.hasKeyword(Keyword.HASTE)).isTrue();
    }
}
