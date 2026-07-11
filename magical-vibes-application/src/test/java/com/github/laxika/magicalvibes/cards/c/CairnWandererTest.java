package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.w.WallOfBone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CairnWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Gains a watched keyword from a creature card in the controller's graveyard")
    void gainsKeywordFromOwnGraveyard() {
        Permanent wanderer = addWanderer(player1);
        // Serra Angel has flying and vigilance
        harness.setGraveyard(player1, new ArrayList<>(List.of(new SerraAngel())));

        var keywords = gqs.computeStaticBonus(gd, wanderer).keywords();

        assertThat(keywords).contains(Keyword.FLYING, Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Gains a watched keyword from a creature card in an opponent's graveyard")
    void gainsKeywordFromOpponentGraveyard() {
        Permanent wanderer = addWanderer(player1);
        harness.setGraveyard(player2, new ArrayList<>(List.of(new GiantSpider())));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).contains(Keyword.REACH);
    }

    @Test
    @DisplayName("Gains the exact landwalk variant present in a graveyard")
    void gainsLandwalkVariant() {
        Permanent wanderer = addWanderer(player1);
        // Bog Wraith has swampwalk
        harness.setGraveyard(player1, new ArrayList<>(List.of(new BogWraith())));

        var keywords = gqs.computeStaticBonus(gd, wanderer).keywords();

        assertThat(keywords).contains(Keyword.SWAMPWALK);
        assertThat(keywords).doesNotContain(Keyword.FORESTWALK, Keyword.ISLANDWALK, Keyword.MOUNTAINWALK);
    }

    @Test
    @DisplayName("Combines watched keywords from creature cards across all graveyards")
    void combinesKeywordsFromAllGraveyards() {
        Permanent wanderer = addWanderer(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new SerraAngel())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new AvatarOfMight())));

        var keywords = gqs.computeStaticBonus(gd, wanderer).keywords();

        assertThat(keywords).contains(Keyword.FLYING, Keyword.VIGILANCE, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Does not gain keywords outside the watched list")
    void doesNotGainUnwatchedKeyword() {
        Permanent wanderer = addWanderer(player1);
        // Wall of Bone has defender, which Cairn Wanderer does not grant
        harness.setGraveyard(player1, new ArrayList<>(List.of(new WallOfBone())));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).doesNotContain(Keyword.DEFENDER);
    }

    @Test
    @DisplayName("Gains nothing from vanilla creature cards in a graveyard")
    void gainsNothingFromVanillaCreature() {
        Permanent wanderer = addWanderer(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords())
                .doesNotContain(Keyword.FLYING, Keyword.TRAMPLE, Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Loses the keyword when the creature card leaves the graveyard")
    void losesKeywordWhenCardLeavesGraveyard() {
        Permanent wanderer = addWanderer(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new SerraAngel())));
        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).contains(Keyword.FLYING);

        gd.playerGraveyards.get(player1.getId()).clear();

        assertThat(gqs.computeStaticBonus(gd, wanderer).keywords()).doesNotContain(Keyword.FLYING);
    }

    private Permanent addWanderer(Player player) {
        Permanent perm = new Permanent(new CairnWanderer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
