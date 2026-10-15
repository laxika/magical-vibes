package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasElite.class, GrizzlyBears.class, Plains.class, DarksteelAxe.class, FieldCreeper.class})
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
        Permanent elite = harness.addToBattlefieldAndReturn(player, new LilianasElite());
        elite.setSummoningSick(false);
        return elite;
    }

    @Test
    @DisplayName("Has no bonus with an empty graveyard")
    void hasNoBonusWithEmptyGraveyard() {
        Permanent elite = addEliteReady(player1);
        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses its bonus as creature cards leave the graveyard")
    void updatesWhenCreatureCardsLeaveGraveyard() {
        Permanent elite = addEliteReady(player1);
        harness.setGraveyard(player1, List.of(new LilianasElite(), new LilianasElite()));

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(2);

        gd.playerGraveyards.get(player1.getId()).clear();

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts an artifact creature card once")
    void countsArtifactCreatureCardOnce() {
        Permanent elite = addEliteReady(player1);
        harness.setGraveyard(player1, List.of(new FieldCreeper()));

        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elite)).isEqualTo(2);
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GrizzlyBears());
        }
        return creatures;
    }
}
