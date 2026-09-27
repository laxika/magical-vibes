package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbedSpike.class, GrizzlyBears.class})
class BarbedSpikeTest extends BaseCardTest {

    @Test
    void createsAndEquipsAThopter() {
        castAndResolveBarbedSpike();

        Permanent spike = findPermanent(player1, "Barbed Spike");
        Permanent thopter = findPermanent(player1, "Thopter");

        assertThat(spike.getAttachedTo()).isEqualTo(thopter.getId());
        assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    @Test
    void equipMovesBarbedSpikeOffItsThopter() {
        castAndResolveBarbedSpike();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent spike = findPermanent(player1, "Barbed Spike");
        Permanent thopter = findPermanent(player1, "Thopter");

        assertThat(spike.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    private void castAndResolveBarbedSpike() {
        harness.setHand(player1, List.of(new BarbedSpike()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
