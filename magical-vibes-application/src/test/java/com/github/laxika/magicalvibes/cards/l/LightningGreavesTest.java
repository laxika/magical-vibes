package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LightningGreavesTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has haste and shroud")
    void equippedCreatureHasHasteAndShroud() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();

        greaves.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Creature loses haste and shroud when Greaves are unattached")
    void creatureLosesKeywordsWhenGreavesAreUnattached() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        greaves.setAttachedTo(creature.getId());

        greaves.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Zero-cost equip attaches Greaves to a creature")
    void zeroCostEquipAttachesGreaves() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(greaves.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addGreavesReady(Player player) {
        Permanent permanent = new Permanent(new LightningGreaves());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
