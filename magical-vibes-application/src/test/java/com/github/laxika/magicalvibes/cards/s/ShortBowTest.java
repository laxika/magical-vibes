package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShortBow.class, GrizzlyBears.class})
class ShortBowTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {1} attaches Short Bow to target creature you control")
    void equipAttachesToCreature() {
        Permanent bow = addBowReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 and has reach and vigilance")
    void equippedCreatureGetsBoostAndKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bow = addBowReady(player1);
        bow.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Unattached Short Bow does not affect creatures")
    void unattachedBowGrantsNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addBowReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses Short Bow's effects when it becomes unattached")
    void effectsStopWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bow = addBowReady(player1);
        bow.setAttachedTo(creature.getId());

        bow.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addBowReady(Player player) {
        Permanent permanent = new Permanent(new ShortBow());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
