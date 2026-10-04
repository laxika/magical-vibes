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
        Permanent aura1 = addCreatureReady(player1, new Pacifism());
        Permanent aura2 = addCreatureReady(player1, new Pacifism());
        aura1.setAttachedTo(artisan.getId());
        aura2.setAttachedTo(artisan.getId());

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(7);
    }

    @Test
    void doesNotCountAttachedEquipment() {
        Permanent artisan = addArtisan(player1);
        Permanent equipment = addCreatureReady(player1, new LeoninScimitar());
        equipment.setAttachedTo(artisan.getId());

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(4);
    }

    @Test
    void countsAnAuraControlledByOpponent() {
        Permanent artisan = addArtisan(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(artisan.getId());

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(5);
    }

    @Test
    void bonusFollowsAuraWhenItMovesToAnotherCreature() {
        Permanent artisan = addArtisan(player1);
        Permanent otherArtisan = addArtisan(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(artisan.getId());

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherArtisan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherArtisan)).isEqualTo(3);

        aura.setAttachedTo(otherArtisan.getId());

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherArtisan)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherArtisan)).isEqualTo(5);
    }

    @Test
    void losesOnlyTheBonusFromAnAuraThatLeavesTheBattlefield() {
        Permanent artisan = addArtisan(player1);
        Permanent aura1 = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        Permanent aura2 = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura1.setAttachedTo(artisan.getId());
        aura2.setAttachedTo(artisan.getId());

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(7);

        gd.playerBattlefields.get(player2.getId()).remove(aura2);
        gd.playerGraveyards.get(player2.getId()).add(aura2.getCard());

        assertThat(gqs.getEffectivePower(gd, artisan)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artisan)).isEqualTo(5);
    }

    private Permanent addArtisan(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GracebladeArtisan());
    }
}
