package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
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

class LordOfExtinctionTest extends BaseCardTest {

    @Test
    @DisplayName("Lord of Extinction is 0/0 with no cards in any graveyard")
    void isZeroZeroWithEmptyGraveyards() {
        Permanent perm = addLordReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Lord of Extinction P/T counts cards of every type in a graveyard")
    void ptCountsEveryCardType() {
        Permanent perm = addLordReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GrizzlyBears()); // creature
        graveyard.add(new Plains());       // land
        graveyard.add(new MindRot());      // sorcery
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lord of Extinction P/T counts cards in ALL graveyards")
    void ptCountsAllGraveyards() {
        Permanent perm = addLordReady(player1);
        harness.setGraveyard(player1, createCards(2));
        harness.setGraveyard(player2, createCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Lord of Extinction P/T updates as cards enter graveyards")
    void ptUpdatesWhenGraveyardChanges() {
        Permanent perm = addLordReady(player1);
        harness.setGraveyard(player1, createCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lord of Extinction P/T stacks with temporary modifiers")
    void ptStacksWithTemporaryModifiers() {
        Permanent perm = addLordReady(player1);
        harness.setGraveyard(player1, createCards(4));

        perm.setPowerModifier(2);
        perm.setToughnessModifier(2);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(6);
    }

    private Permanent addLordReady(Player player) {
        Permanent perm = new Permanent(new LordOfExtinction());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Card> createCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
