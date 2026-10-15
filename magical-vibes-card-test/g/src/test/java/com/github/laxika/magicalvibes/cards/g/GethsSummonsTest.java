package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GethsSummons.class, GrizzlyBears.class})
class GethsSummonsTest extends BaseCardTest {

    @Test
    void returnsACreatureFromYourGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castSummons();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(creature.getId());
    }

    @Test
    void corruptedReturnsACreatureFromAnOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castSummons();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(creature.getId());
    }

    @Test
    void corruptedUsesPoisonCountersAsCastRatherThanOnResolution() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castSummons();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        gd.playerPoisonCounters.put(player2.getId(), 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(creature.getId());
    }

    @Test
    void doesNotOfferAnOpponentsCreatureWithoutThreePoisonCounters() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        gd.playerPoisonCounters.put(player2.getId(), 2);
        castSummons();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(creature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
    }

    private void castSummons() {
        harness.setHand(player1, List.of(new GethsSummons()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
    }
}
