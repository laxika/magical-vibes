package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necroplasm.class, GrizzlyBears.class, SavannahLions.class, Forest.class})
class NecroplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Its upkeep trigger puts a +1/+1 counter on it")
    void putsCounterOnItselfAtUpkeep() {
        Permanent necroplasm = addNecroplasm(player1);

        triggerUpkeep(player1);
        harness.passBothPriorities();

        assertThat(necroplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its end-step trigger destroys creatures with matching mana value")
    void destroysCreaturesWithMatchingManaValue() {
        Permanent necroplasm = addNecroplasm(player1);
        necroplasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLions = harness.addToBattlefieldAndReturn(player2, new SavannahLions());

        triggerEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownBears);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opponentBears)
                .contains(opponentLions);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The upkeep counter is counted by that turn's end-step trigger")
    void upkeepCounterIsCountedAtEndStep() {
        Permanent necroplasm = addNecroplasm(player1);
        necroplasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerUpkeep(player1);
        harness.passBothPriorities();
        triggerEndStep(player1);

        assertThat(necroplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Dredge 2 returns Necroplasm instead of drawing")
    void dredgesInsteadOfDrawing() {
        Necroplasm necroplasm = new Necroplasm();
        List<Card> milled = List.of(new Forest(), new GrizzlyBears());
        harness.setGraveyard(player1, List.of(necroplasm));
        harness.setLibrary(player1, milled);

        resolveDraw();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(necroplasm);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
    }

    @Test
    @DisplayName("Declining dredge draws normally")
    void declinesDredge() {
        Necroplasm necroplasm = new Necroplasm();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(necroplasm));
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears()));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(necroplasm);
    }

    private Permanent addNecroplasm(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Necroplasm());
    }

    private void triggerUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void triggerEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
