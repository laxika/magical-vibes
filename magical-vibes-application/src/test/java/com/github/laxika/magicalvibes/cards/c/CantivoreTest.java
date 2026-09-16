package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Standstill;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cantivore.class, Standstill.class, Plains.class})
class CantivoreTest extends BaseCardTest {

    @Test
    @DisplayName("Cantivore is 0/0 when all graveyards are empty of enchantments")
    void isZeroZeroWithoutEnchantments() {
        Permanent cantivore = addCreatureReady(player1, new Cantivore());

        assertThat(gqs.getEffectivePower(gd, cantivore)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, cantivore)).isZero();
    }

    @Test
    @DisplayName("Cantivore's power and toughness count enchantment cards in all graveyards")
    void countsEnchantmentsInAllGraveyards() {
        Permanent cantivore = addCreatureReady(player1, new Cantivore());
        harness.setGraveyard(player1, List.of(new Standstill(), new Cantivore()));
        harness.setGraveyard(player2, List.of(new Standstill(), new Plains()));

        assertThat(gqs.getEffectivePower(gd, cantivore)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cantivore)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cantivore updates as enchantment cards enter and leave graveyards")
    void updatesWithGraveyardChanges() {
        Permanent cantivore = addCreatureReady(player1, new Cantivore());
        harness.setGraveyard(player1, List.of(new Standstill(), new Cantivore()));

        assertThat(gqs.getEffectivePower(gd, cantivore)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cantivore)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new Standstill(), new Cantivore(), new Standstill()));
        assertThat(gqs.getEffectivePower(gd, cantivore)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cantivore)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new Cantivore(), new Standstill()));
        assertThat(gqs.getEffectivePower(gd, cantivore)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cantivore)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cantivore's vigilance keeps it untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        harness.setGraveyard(player1, List.of(new Standstill()));
        Permanent cantivore = addCreatureReady(player1, new Cantivore());

        declareAttackers(List.of(0));

        assertThat(cantivore.isTapped()).isFalse();
    }
}
