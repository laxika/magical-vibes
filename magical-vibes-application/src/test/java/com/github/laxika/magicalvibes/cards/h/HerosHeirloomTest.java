package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HerosHeirloom.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class HerosHeirloomTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent heirloom = addHeirloomReady(player1);
        heirloom.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equipped legendary creature has trample and haste")
    void equippedLegendaryCreatureHasKeywords() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent heirloom = addHeirloomReady(player1);
        heirloom.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Bonuses are lost when Hero's Heirloom becomes unattached")
    void bonusesAreLostWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent heirloom = addHeirloomReady(player1);
        heirloom.setAttachedTo(creature.getId());

        heirloom.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Hero's Heirloom to a creature you control")
    void equipAttachesToCreature() {
        Permanent heirloom = addHeirloomReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(heirloom.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addHeirloomReady(Player player) {
        Permanent perm = new Permanent(new HerosHeirloom());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
