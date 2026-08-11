package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TerravoreTest extends BaseCardTest {

    @Test
    @DisplayName("Terravore is 0/0 with no land cards in any graveyard")
    void isZeroZeroWithNoLandsInGraveyards() {
        Permanent terravore = addTerravoreReady(player1);

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(0);
    }

    @Test
    @DisplayName("Terravore counts land cards in all graveyards")
    void countsLandsInAllGraveyards() {
        Permanent terravore = addTerravoreReady(player1);
        harness.setGraveyard(player1, List.of(new Plains(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Plains(), new Plains()));

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(3);
    }

    @Test
    @DisplayName("Terravore updates as land cards enter any graveyard")
    void updatesWhenGraveyardsChange() {
        Permanent terravore = addTerravoreReady(player1);
        harness.setGraveyard(player1, List.of(new Plains()));

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(1);

        gd.playerGraveyards.get(player2.getId()).add(new Plains());

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(2);
    }

    @Test
    @DisplayName("Terravore's dynamic power and toughness stack with modifiers")
    void stacksWithModifiers() {
        Permanent terravore = addTerravoreReady(player1);
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));

        terravore.setPowerModifier(2);
        terravore.setToughnessModifier(2);

        assertThat(gqs.getEffectivePower(gd, terravore)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, terravore)).isEqualTo(4);
    }

    private Permanent addTerravoreReady(Player player) {
        Permanent permanent = new Permanent(new Terravore());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
