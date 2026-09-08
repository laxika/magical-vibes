package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BattleOfFrostAndFireTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I damages non-Giants and planeswalkers but not Giants")
    void chapterIDamagesNonGiantsAndPlaneswalkers() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleOfFrostAndFire());
        saga.setCounterCount(CounterType.LORE, 0);
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1,
                new com.github.laxika.magicalvibes.cards.h.HillGiant());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, testPlaneswalker());
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

        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
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
        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

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

    private Card testPlaneswalker() {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setManaCost("{3}");
        card.setLoyalty(5);
        return card;
    }
}
