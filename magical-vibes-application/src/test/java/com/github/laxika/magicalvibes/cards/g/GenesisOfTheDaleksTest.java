package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cybermat;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentFacesGenesisVillainousChoiceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenesisOfTheDaleks.class, Cybermat.class})
class GenesisOfTheDaleksTest extends BaseCardTest {

    @Test
    void chaptersCreateDaleksForEachLoreCounter() {
        Permanent saga = addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();
        assertThat(findDaleks()).hasSize(1);

        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter();
        harness.passBothPriorities();
        assertThat(findDaleks()).hasSize(3);

        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        harness.passBothPriorities();
        assertThat(findDaleks()).hasSize(6);
    }

    @Test
    void opponentCanChooseDalekDestructionAndLifeLoss() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent nonDalek = harness.addToBattlefieldAndReturn(player1, new Cybermat());
        Permanent dalek = findDaleks().getFirst();
        saga.setCounterCount(CounterType.LORE, 3);
        harness.setLife(player2, 20);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player2, TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_DALEKS);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dalek);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonDalek);
        harness.assertLife(player2, 17);
    }

    @Test
    void opponentCanChooseNonDalekDestruction() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent nonDalek = harness.addToBattlefieldAndReturn(player1, new Cybermat());
        Permanent dalek = findDaleks().getFirst();
        saga.setCounterCount(CounterType.LORE, 3);
        harness.setLife(player2, 20);

        advanceToNextChapter();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_NON_DALEKS);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dalek);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nonDalek);
        harness.assertLife(player2, 20);
    }

    @Test
    void chapterCountsLoreCountersWhenItResolves() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        saga.setCounterCount(CounterType.LORE, 3);

        harness.passBothPriorities();

        assertThat(findDaleks()).hasSize(3);
    }

    @Test
    void chapterUsesLastKnownLoreCountersAfterSagaLeaves() {
        Permanent saga = addSagaWithLore(1);
        advanceToNextChapter();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, saga));

        harness.passBothPriorities();

        assertThat(findDaleks()).hasSize(2);
        harness.assertInGraveyard(player1, "Genesis of the Daleks");
    }

    @Test
    void dalekChoiceIncludesEarlierDeathsAndPowerAtDeath() {
        Permanent saga = addSagaWithLore(1);
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent earlierDeath = findDaleks().getFirst();
        earlierDeath.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, earlierDeath));
        saga.setCounterCount(CounterType.LORE, 3);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_DALEKS);

        assertThat(findDaleks()).isEmpty();
        harness.assertLife(player2, 12);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Genesis of the Daleks");
    }

    @Test
    void nonDalekChoiceDestroysCreaturesOnBothBattlefieldsAndSparesDaleks() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Cybermat());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new Cybermat());
        Permanent opposingSaga = harness.enterBattlefieldAndReturn(player2, new GenesisOfTheDaleks());
        harness.passBothPriorities();
        saga.setCounterCount(CounterType.LORE, 3);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_NON_DALEKS);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature)
                .contains(opposingSaga).anyMatch(p -> p.getCard().getName().equals("Dalek"));
        assertThat(findDaleks()).hasSize(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void dalekChoiceCountsDaleksControlledByEitherPlayer() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent opposingSaga = harness.enterBattlefieldAndReturn(player2, new GenesisOfTheDaleks());
        harness.passBothPriorities();
        saga.setCounterCount(CounterType.LORE, 3);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_DALEKS);

        assertThat(findDaleks()).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingSaga)
                .noneMatch(p -> p.getCard().getName().equals("Dalek"));
        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
    }

    @Test
    void opponentCanChooseDalekDestructionWithNoDaleks() {
        addSagaWithLore(3);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Cybermat());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_DALEKS);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Genesis of the Daleks");
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new GenesisOfTheDaleks());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private java.util.List<Permanent> findDaleks() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Dalek"))
                .toList();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }
}
