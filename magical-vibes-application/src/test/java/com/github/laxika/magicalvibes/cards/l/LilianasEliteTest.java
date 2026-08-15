package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LilianasEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each creature card in its controller's graveyard")
    void getsBoostForCreatureCardsInControllerGraveyard() {
        Permanent elite = addEliteReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count noncreature cards or cards in an opponent's graveyard")
    void ignoresNoncreatureCardsAndOpponentGraveyard() {
        Permanent elite = addEliteReady(player1);

        List<Card> ownGraveyard = new ArrayList<>(createCreatureCards(2));
        ownGraveyard.add(new Plains());
        ownGraveyard.add(new DarksteelAxe());
        harness.setGraveyard(player1, ownGraveyard);
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(3);
    }

    @Test
    @DisplayName("Updates when a creature card enters its controller's graveyard")
    void updatesWhenCreatureCardIsAddedToGraveyard() {
        Permanent elite = addEliteReady(player1);
        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(2);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(3);
    }

    private Permanent addEliteReady(Player player) {
        Permanent elite = new Permanent(new LilianasElite());
        elite.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(elite);
        return elite;
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GrizzlyBears());
        }
        return creatures;
    }
}
