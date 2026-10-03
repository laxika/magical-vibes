package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NikoAris;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleOfFrostAndFire.class, ColossalDreadmaw.class, Fireball.class, Forest.class,
        GrizzlyBears.class, HillGiant.class, NikoAris.class, Shock.class})
class BattleOfFrostAndFireTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I damages non-Giants and planeswalkers but not Giants")
    void chapterIDamagesNonGiantsAndPlaneswalkers() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleOfFrostAndFire());
        saga.setCounterCount(CounterType.LORE, 0);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1,
                new HillGiant());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NikoAris());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        advanceToChapter();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId())).contains(giant);
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II scries three cards")
    void chapterIIScriesThree() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleOfFrostAndFire());
        saga.setCounterCount(CounterType.LORE, 1);
        harness.setLibrary(player1, List.of(new Shock(), new Forest(), new Shock()));

        advanceToChapter();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(3);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chapter III keeps drawing and discarding after the Saga is sacrificed")
    void chapterIIITriggersAfterSagaLeaves() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleOfFrostAndFire());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new Shock(), new Forest()));

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);

        harness.castFromHand(player1, new ColossalDreadmaw(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Chapter III uses a spell's chosen X value for its mana value")
    void chapterIIIUsesChosenXValue() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleOfFrostAndFire());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new Shock(), new Forest()));

        advanceToChapter();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, 4, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void advanceToChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Chapter I does not damage either player")
    void chapterIDoesNotDamagePlayers() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleOfFrostAndFire());
        saga.setCounterCount(CounterType.LORE, 0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToChapter();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Chapter I triggers when the Saga enters")
    void chapterITriggersOnEntry() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new BattleOfFrostAndFire(), "{3}{U}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Chapter III does not trigger for mana value four")
    void chapterIIIDoesNotTriggerBelowThreshold() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleOfFrostAndFire());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        advanceToChapter();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chapter III triggers for every qualifying spell in the turn")
    void chapterIIITriggersRepeatedly() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleOfFrostAndFire());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        advanceToChapter();
        harness.passBothPriorities();

        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new ColossalDreadmaw(), "{4}{G}{G}");
            assertThat(gd.stack).hasSize(2);
            harness.passBothPriorities();
            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            harness.handleCardChosen(player1, 0);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Chapter III ignores qualifying spells cast by the opponent")
    void chapterIIIIgnoresOpponentSpells() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleOfFrostAndFire());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        advanceToChapter();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ColossalDreadmaw(), "{4}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Chapter III stops triggering after the turn ends")
    void chapterIIIExpiresAtEndOfTurn() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleOfFrostAndFire());
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        advanceToChapter();
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ColossalDreadmaw(), "{4}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
