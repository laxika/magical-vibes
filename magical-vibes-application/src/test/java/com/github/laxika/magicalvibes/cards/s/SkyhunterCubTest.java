package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningGreaves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyhunterCub.class, LightningGreaves.class})
class SkyhunterCubTest extends BaseCardTest {

    @Test
    void withoutEquipmentHasNoBonus() {
        Permanent cub = addCreatureReady(player1, new SkyhunterCub());

        assertThat(gqs.getEffectivePower(gd, cub)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cub)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cub, Keyword.FLYING)).isFalse();
    }

    @Test
    void whileEquippedGetsBonusAndFlying() {
        Permanent cub = addCreatureReady(player1, new SkyhunterCub());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LightningGreaves());
        equipment.setAttachedTo(cub.getId());

        assertThat(gqs.getEffectivePower(gd, cub)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cub)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cub, Keyword.FLYING)).isTrue();
    }

    @Test
    void equipmentAttachedToAnotherCreatureDoesNotEnhanceCub() {
        Permanent cub = addCreatureReady(player1, new SkyhunterCub());
        Permanent otherCub = addCreatureReady(player1, new SkyhunterCub());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LightningGreaves());
        equipment.setAttachedTo(otherCub.getId());

        assertThat(gqs.getEffectivePower(gd, cub)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cub)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cub, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, otherCub)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherCub)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, otherCub, Keyword.FLYING)).isTrue();
    }

    @Test
    void losesBonusWhenEquipmentIsDetached() {
        Permanent cub = addCreatureReady(player1, new SkyhunterCub());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LightningGreaves());
        equipment.setAttachedTo(cub.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, cub)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cub)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cub, Keyword.FLYING)).isFalse();
    }
}
