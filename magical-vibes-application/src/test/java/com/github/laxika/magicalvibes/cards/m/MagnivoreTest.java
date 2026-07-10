package com.github.laxika.magicalvibes.cards.m;

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

class MagnivoreTest extends BaseCardTest {

    @Test
    @DisplayName("Magnivore is 0/0 with no sorcery cards in any graveyard")
    void isZeroZeroWithEmptyGraveyards() {
        Permanent perm = addMagnivoreReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Magnivore P/T equals number of sorcery cards in controller's graveyard")
    void ptEqualsSorceryCountInOwnGraveyard() {
        Permanent perm = addMagnivoreReady(player1);
        harness.setGraveyard(player1, createSorceryCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Magnivore P/T counts sorcery cards in ALL graveyards")
    void ptCountsAllGraveyards() {
        Permanent perm = addMagnivoreReady(player1);
        harness.setGraveyard(player1, createSorceryCards(2));
        harness.setGraveyard(player2, createSorceryCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Magnivore only counts sorcery cards, not other card types")
    void onlyCountsSorceryCards() {
        Permanent perm = addMagnivoreReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createSorceryCards(2));
        graveyard.add(new Plains());
        graveyard.add(new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Magnivore P/T updates when a sorcery is added to a graveyard")
    void ptUpdatesWhenSorceryAdded() {
        Permanent perm = addMagnivoreReady(player1);
        harness.setGraveyard(player1, createSorceryCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new MindRot());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    // ===== Helper methods =====

    private Permanent addMagnivoreReady(Player player) {
        Magnivore card = new Magnivore();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Card> createSorceryCards(int count) {
        List<Card> sorceries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            sorceries.add(new MindRot());
        }
        return sorceries;
    }
}
