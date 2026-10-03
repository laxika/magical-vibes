package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AeonEngine.class, TimeWarp.class})
class AeonEngineTest extends BaseCardTest {

    @Test
    void entersTappedAndReversesTurnOrderWhenActivated() {
        AeonEngine card = new AeonEngine();
        harness.castFromHand(player1, card, "{5}");
        harness.passBothPriorities();

        Permanent engine = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(engine.isTapped()).isTrue();

        List<UUID> originalOrder = List.copyOf(gd.orderedPlayerIds);
        engine.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.orderedPlayerIds).containsExactly(originalOrder.get(1), originalOrder.get(0));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    void tappedEngineCannotPayItsActivationCost() {
        Permanent engine = harness.enterBattlefieldAndReturn(player1, new AeonEngine());
        List<UUID> originalOrder = List.copyOf(gd.orderedPlayerIds);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(engine.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(engine);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.orderedPlayerIds).containsExactlyElementsOf(originalOrder);
    }

    @Test
    void exileIsPaidImmediatelyButReversalWaitsForResolution() {
        AeonEngine card = new AeonEngine();
        harness.addToBattlefield(player1, card);
        List<UUID> originalOrder = List.copyOf(gd.orderedPlayerIds);
        UUID activePlayer = gd.activePlayerId;

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.orderedPlayerIds).containsExactlyElementsOf(originalOrder);

        harness.passBothPriorities();

        assertThat(gd.orderedPlayerIds).containsExactly(originalOrder.get(1), originalOrder.get(0));
        assertThat(gd.activePlayerId).isEqualTo(activePlayer);
    }

    @Test
    void reversingTwiceRestoresOriginalOrder() {
        harness.addToBattlefield(player1, new AeonEngine());
        harness.addToBattlefield(player1, new AeonEngine());
        List<UUID> originalOrder = List.copyOf(gd.orderedPlayerIds);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.orderedPlayerIds).containsExactlyElementsOf(originalOrder);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    void reversalDuringOpponentsExtraTurnResumesAfterLastNormalTurnPlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new AeonEngine());
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentTurnIsExtraTurn).isTrue();

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.currentTurnIsExtraTurn).isFalse();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }
}
