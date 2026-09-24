package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheAnimus.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class TheAnimusTest extends BaseCardTest {

    @Test
    void exilesUpToOneLegendaryCreatureFromAnyGraveyardWithMemoryCounter() {
        Card legendaryCreature = new IsamaruHoundOfKonda();
        harness.setGraveyard(player2, List.of(legendaryCreature));
        addCreatureReady(player1, new TheAnimus());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(legendaryCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(legendaryCreature.getId());
        assertThat(gd.exiledCardsWithMemoryCounters).contains(legendaryCreature.getId());
    }

    @Test
    void makesTargetLegendaryCreatureCopyMemoryMarkedExiledCreatureUntilNextTurn() {
        Card exiledCreature = new GrizzlyBears();
        harness.setExile(player1, List.of(exiledCreature));
        gd.exiledCardsWithMemoryCounters.add(exiledCreature.getId());
        addCreatureReady(player1, new TheAnimus());
        Permanent legendaryCreature = addCreatureReady(player1, new IsamaruHoundOfKonda());

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(legendaryCreature.getId(), exiledCreature.getId()));
        harness.passBothPriorities();

        assertThat(legendaryCreature.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(legendaryCreature.isCopyUntilControllerNextTurn()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isTrue();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
