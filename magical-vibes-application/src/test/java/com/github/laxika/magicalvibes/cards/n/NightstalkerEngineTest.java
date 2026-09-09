package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GoldenBear;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightstalkerEngine.class, GoldenBear.class, Plains.class})
class NightstalkerEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Power is 0 with an empty graveyard; toughness stays 3")
    void powerZeroWithEmptyGraveyard() {
        Permanent engine = addCreatureReady(player1, new NightstalkerEngine());

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power equals the number of creature cards in your graveyard; toughness stays 3")
    void powerEqualsCreatureCardsInOwnGraveyard() {
        Permanent engine = addCreatureReady(player1, new NightstalkerEngine());
        harness.setGraveyard(player1, createCreatureCards(4));

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only creature cards count, not non-creature cards")
    void onlyCountsCreatureCards() {
        Permanent engine = addCreatureReady(player1, new NightstalkerEngine());

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(2));
        graveyard.add(new Plains());
        graveyard.add(new Plains());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count creature cards in an opponent's graveyard")
    void doesNotCountOpponentsGraveyard() {
        Permanent engine = addCreatureReady(player1, new NightstalkerEngine());
        harness.setGraveyard(player1, createCreatureCards(1));
        harness.setGraveyard(player2, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power updates as creature cards enter the graveyard")
    void powerUpdatesWhenCreaturesAdded() {
        Permanent engine = addCreatureReady(player1, new NightstalkerEngine());
        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new GoldenBear());

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power updates as creature cards leave the graveyard")
    void powerUpdatesWhenCreaturesRemoved() {
        Permanent engine = addCreatureReady(player1, new NightstalkerEngine());
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).remove(0);

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power counts the graveyard of the permanent's controller")
    void powerCountsControllerGraveyard() {
        Permanent engine = addCreatureReady(player2, new NightstalkerEngine());
        harness.setGraveyard(player1, createCreatureCards(1));
        harness.setGraveyard(player2, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(3);
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GoldenBear());
        }
        return creatures;
    }
}
