package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampiresVengeance.class, CaptivatingVampire.class, GiantSpider.class})
class VampiresVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each non-Vampire creature and creates a Blood token")
    void damagesNonVampireCreaturesAndCreatesBlood() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent nonVampire = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new VampiresVengeance()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(vampire.getMarkedDamage()).isZero();
        assertThat(nonVampire.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }
}
