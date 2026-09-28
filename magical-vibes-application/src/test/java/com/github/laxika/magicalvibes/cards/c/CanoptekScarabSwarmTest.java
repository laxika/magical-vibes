package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanoptekScarabSwarm.class, Frogmite.class, Forest.class, GrizzlyBears.class})
class CanoptekScarabSwarmTest extends BaseCardTest {

    @Test
    void exilesTargetPlayersGraveyardAndCreatesAnInsectForEachArtifactOrLand() {
        Card artifact = new Frogmite();
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(artifact, land, creature));

        castCanoptekScarabSwarm(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(artifact, land, creature);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(2);
    }

    @Test
    void createsNoTokensWhenTargetPlayersGraveyardHasNoArtifactsOrLands() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        castCanoptekScarabSwarm(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .isEmpty();
    }

    private void castCanoptekScarabSwarm(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new CanoptekScarabSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
