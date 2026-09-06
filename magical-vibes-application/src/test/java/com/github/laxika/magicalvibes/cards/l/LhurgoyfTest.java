package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
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

@CardUsed({Lhurgoyf.class, BalduvianBears.class, Plains.class})
class LhurgoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Lhurgoyf is 0/1 with no creature cards in any graveyard")
    void isZeroOneWithEmptyGraveyards() {
        Permanent perm = addCreatureReady(player1, new Lhurgoyf());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lhurgoyf power equals creature cards in graveyard; toughness is one more")
    void ptFromOwnGraveyard() {
        Permanent perm = addCreatureReady(player1, new Lhurgoyf());
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Lhurgoyf counts creature cards in ALL graveyards")
    void ptCountsAllGraveyards() {
        Permanent perm = addCreatureReady(player1, new Lhurgoyf());
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.setGraveyard(player2, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Lhurgoyf only counts creature cards, not other card types")
    void onlyCountsCreatureCards() {
        Permanent perm = addCreatureReady(player1, new Lhurgoyf());

        List<Card> graveyard = new ArrayList<>(createCreatureCards(2));
        graveyard.add(new Plains());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lhurgoyf P/T updates as creatures enter the graveyard")
    void ptUpdatesWithGraveyard() {
        Permanent perm = addCreatureReady(player1, new Lhurgoyf());
        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);

        gd.playerGraveyards.get(player1.getId()).add(new BalduvianBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lhurgoyf P/T updates as creature cards leave the graveyard")
    void ptUpdatesWhenCreatureLeavesGraveyard() {
        Permanent perm = addCreatureReady(player1, new Lhurgoyf());
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);

        gd.playerGraveyards.get(player1.getId()).remove(0);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new BalduvianBears());
        }
        return creatures;
    }
}
