package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TouchOfDeathTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new TouchOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3); // {2}{B}
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 1 damage to the target player and controller gains 1 life")
    void dealsDamageAndGainsLife() {
        int targetBefore = gd.getLife(player2.getId());
        int controllerBefore = gd.getLife(player1.getId());

        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(targetBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerBefore + 1);
    }

    @Test
    @DisplayName("Deals 1 damage to a targeted planeswalker")
    void dealsDamageToTargetPlaneswalker() {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(2);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);

        int targetLifeBefore = gd.getLife(player2.getId());
        castWithTarget(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(targetLifeBefore);
    }

    @Test
    @DisplayName("Schedules a draw at the next upkeep, resolving there")
    void schedulesAndResolvesDraw() {
        GameData localGd = harness.getGameData();

        cast();

        List<DrawCardsAtNextUpkeep> scheduled = localGd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());

        int handBefore = localGd.playerHands.get(player1.getId()).size();
        int deckBefore = localGd.playerDecks.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        localGd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(localGd));
        harness.passBothPriorities();

        assertThat(localGd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(localGd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(localGd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    private void castWithTarget(UUID targetId) {
        harness.setHand(player1, List.of(new TouchOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 3); // {2}{B}
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
