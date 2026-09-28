package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentFacesGenesisVillainousChoiceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenesisOfTheDaleks.class, GrizzlyBears.class})
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
        Permanent nonDalek = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        Permanent nonDalek = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
