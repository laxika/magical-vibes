package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.c.CravenHulk;
import com.github.laxika.magicalvibes.cards.s.SnakeskinVeil;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FallOfTheImpostor.class, BeskirShieldmate.class, CravenHulk.class, SnakeskinVeil.class})
class FallOfTheImpostorTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void counterChaptersExcludeOpposingHexproofCreatures(int previousLoreCount) {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BeskirShieldmate());
        harness.setHand(player2, List.of(new SnakeskinVeil()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        Permanent legalCreature = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FallOfTheImpostor());
        saga.setCounterCount(CounterType.LORE, previousLoreCount);
        beginNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(legalCreature.getId()).doesNotContain(creature.getId());
        harness.handlePermanentChosen(player1, legalCreature.getId());
        harness.passBothPriorities();
        assertThat(legalCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void chapterICanChooseNoCreatureEvenWhenOneIsAvailable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        harness.castFromHand(player1, new FallOfTheImpostor(), "{1}{G}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chapterIICanPutCounterOnOpponentsCreature() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FallOfTheImpostor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BeskirShieldmate());
        saga.setCounterCount(CounterType.LORE, 1);
        beginNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    void chapterIIIUsesCurrentPowerAndIgnoresControllersCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FallOfTheImpostor());
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player2, new BeskirShieldmate());
        Permanent hulk = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        Permanent ownHulk = harness.addToBattlefieldAndReturn(player1, new CravenHulk());
        shieldmate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        ownHulk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        saga.setCounterCount(CounterType.LORE, 2);
        beginNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(shieldmate.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hulk).doesNotContain(shieldmate);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownHulk).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void chapterIIIWithNoOpposingCreaturesStillSacrificesSaga() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FallOfTheImpostor());
        saga.setCounterCount(CounterType.LORE, 2);
        beginNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    private void beginNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Chapter I puts a +1/+1 counter on the chosen creature")
    void chapterIPutsCounterOnChosenCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        harness.castFromHand(player1, new FallOfTheImpostor(), "{1}{G}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III targets an opponent and exiles a creature tied for greatest power")
    void chapterIIIExilesChosenGreatestPowerCreature() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FallOfTheImpostor());
        Permanent firstGiant = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        Permanent secondGiant = harness.addToBattlefieldAndReturn(player2, new CravenHulk());
        harness.addToBattlefield(player2, new BeskirShieldmate());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.validIds()).containsExactly(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SagaChapterPlayerTarget.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        gd = harness.getGameData();
        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.playerId()).isEqualTo(player1.getId());
        assertThat(creatureChoice.validIds()).containsExactlyInAnyOrder(firstGiant.getId(), secondGiant.getId());

        harness.handlePermanentChosen(player1, firstGiant.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Craven Hulk"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(secondGiant.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Beskir Shieldmate"));
    }
}
