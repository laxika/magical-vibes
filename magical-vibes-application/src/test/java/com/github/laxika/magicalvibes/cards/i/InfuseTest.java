package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

class InfuseTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target creature and schedules a draw at the next upkeep")
    void untapsCreatureAndSchedulesDraw() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        Permanent giant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(giantId)).findFirst().orElseThrow();
        giant.tap();

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();

        assertThat(giant.isTapped()).isFalse();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can untap a target land")
    void untapsLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        Permanent forest = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(forestId)).findFirst().orElseThrow();
        forest.tap();

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, forestId);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap a target artifact")
    void untapsArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        Permanent fountain = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(fountainId)).findFirst().orElseThrow();
        fountain.tap();

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, fountainId);
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        int handBefore = gameData.playerHands.get(player1.getId()).size();
        int deckBefore = gameData.playerDecks.get(player1.getId()).size();
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gameData.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gameData));
        harness.passBothPriorities();

        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gameData.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gameData.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Fizzles without scheduling a draw when the target leaves before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, giantId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }
}
