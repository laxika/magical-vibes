package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FrogTongue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CantivoreTest extends BaseCardTest {

    @Test
    @DisplayName("Cantivore is 0/0 when all graveyards are empty of enchantments")
    void isZeroZeroWithoutEnchantments() {
        Permanent cantivore = addCantivoreReady(player1);

        assertThat(gqs.getEffectivePower(gd, cantivore)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, cantivore)).isZero();
    }

    @Test
    @DisplayName("Cantivore's power and toughness count enchantment cards in all graveyards")
    void countsEnchantmentsInAllGraveyards() {
        Permanent cantivore = addCantivoreReady(player1);
        harness.setGraveyard(player1, List.of(new Pacifism(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new FrogTongue(), new Plains()));

        assertThat(gqs.getEffectivePower(gd, cantivore)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cantivore)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cantivore updates as enchantment cards enter and leave graveyards")
    void updatesWithGraveyardChanges() {
        Permanent cantivore = addCantivoreReady(player1);
        harness.setGraveyard(player1, List.of(new Pacifism(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, cantivore)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cantivore)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new FrogTongue());
        assertThat(gqs.getEffectivePower(gd, cantivore)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cantivore)).isEqualTo(2);

        gd.playerGraveyards.get(player1.getId()).removeFirst();
        assertThat(gqs.getEffectivePower(gd, cantivore)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cantivore)).isEqualTo(1);
    }

    private Permanent addCantivoreReady(Player player) {
        Permanent permanent = new Permanent(new Cantivore());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
