package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GracebladeArtisan.class, Pacifism.class, LeoninScimitar.class})
class GracebladeArtisanTest extends BaseCardTest {

    @Test
    void hasBaseStatsWithoutAttachedAuras() {
        Permanent artisan = addArtisan(player1);

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(3);
    }

    @Test
    void getsPlusTwoPlusTwoForEachAttachedAura() {
        Permanent artisan = addArtisan(player1);
        Permanent aura1 = addPermanent(player1, new Pacifism());
        Permanent aura2 = addPermanent(player1, new Pacifism());
        aura1.setAttachedTo(artisan.getId());
        aura2.setAttachedTo(artisan.getId());

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(7);
    }

    @Test
    void doesNotCountAttachedEquipment() {
        Permanent artisan = addArtisan(player1);
        Permanent equipment = addPermanent(player1, new LeoninScimitar());
        equipment.setAttachedTo(artisan.getId());

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(4);
    }

    private Permanent addArtisan(com.github.laxika.magicalvibes.model.Player player) {
        return addPermanent(player, new GracebladeArtisan());
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player,
                                   com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
