package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreyHostReinforcements.class, GrizzlyBears.class, Shock.class})
class GreyHostReinforcementsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target graveyard and gets one counter per creature card exiled")
    void exilesGraveyardAndCountsCreatureCards() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player2, List.of(firstCreature, secondCreature, noncreature));
        castGreyHost(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstCreature, secondCreature, noncreature);
        assertThat(greyHost().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets no counters when the target graveyard has no creature cards")
    void noCreatureCardsMeansNoCounters() {
        Card noncreature = new Shock();
        harness.setGraveyard(player1, List.of(noncreature));
        castGreyHost(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(noncreature);
        assertThat(greyHost().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castGreyHost(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new GreyHostReinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent greyHost() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GreyHostReinforcements)
                .findFirst()
                .orElseThrow();
    }
}
