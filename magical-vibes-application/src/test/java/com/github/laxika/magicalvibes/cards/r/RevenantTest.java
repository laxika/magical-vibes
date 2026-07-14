package com.github.laxika.magicalvibes.cards.r;

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

class RevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Revenant is 0/0 with no creature cards in controller's graveyard")
    void isZeroZeroWithEmptyGraveyard() {
        Permanent perm = addRevenantReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Revenant P/T equals number of creature cards in controller's graveyard")
    void ptEqualsCreatureCountInOwnGraveyard() {
        Permanent perm = addRevenantReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Revenant does NOT count creature cards in opponent's graveyard")
    void doesNotCountOpponentsGraveyard() {
        Permanent perm = addRevenantReady(player1);
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Revenant only counts creature cards, not non-creature cards")
    void onlyCountsCreatureCards() {
        Permanent perm = addRevenantReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(2));
        graveyard.add(new Plains());
        graveyard.add(new DarksteelAxe());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Revenant P/T updates when creatures are added to graveyard")
    void ptUpdatesWhenCreaturesAddedToGraveyard() {
        Permanent perm = addRevenantReady(player1);
        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    private Permanent addRevenantReady(Player player) {
        Revenant card = new Revenant();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GrizzlyBears());
        }
        return creatures;
    }
}
